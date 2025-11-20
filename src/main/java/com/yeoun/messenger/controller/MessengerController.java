package com.yeoun.messenger.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.messenger.dto.*;
import com.yeoun.messenger.entity.MsgMessage;
import com.yeoun.messenger.entity.MsgRelation;
import com.yeoun.messenger.repository.MsgMessageRepository;
import com.yeoun.messenger.repository.MsgRelationRepository;
import com.yeoun.messenger.repository.MsgStatusRepository;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.messenger.service.MessengerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/messenger")
@RequiredArgsConstructor
@Log4j2
public class MessengerController {
	
	private final MessengerService messengerService;
	private final MsgMessageRepository msgMessageRepository;
	private final MsgStatusRepository msgStatusRepository;
	private final MsgRelationRepository msgRelationRepository;
	private final EmpRepository empRepository;

	// ==========================================================================
	// 메신저 큰 화면 (보류)
	@GetMapping(value = {"/", "/index"})
	public String index(Model model) {
		return "/messenger/index";
	}
	
	// ==========================================================================
	// 메신저 팝업 목록 - 친구목록 / 대화목록
	@GetMapping("/list")
	public String list(Authentication authentication, Model model) {
		LoginDTO loginDTO = (LoginDTO)authentication.getPrincipal();
		List<MsgStatusDTO> msgStatusDTOList = messengerService.getUsers(loginDTO.getUsername());
		List<MsgRoomDTO> msgRoomsDTOList = messengerService.getChatRooms(loginDTO.getUsername());
		model.addAttribute("friends", msgStatusDTOList);
		model.addAttribute("rooms", msgRoomsDTOList);
		return "/messenger/list";
	}
	
	// ==========================================================================
	// 메신저 상태 실시간 변경
	@PatchMapping("/status")
	public ResponseEntity<?> changeStatus(Authentication authentication, @RequestBody MsgStatusDTO msgStatusDTO){
		messengerService.updateStatus(authentication.getName(), msgStatusDTO);
		return ResponseEntity.noContent().build();
	}
	
	// ==========================================================================
	// 친구목록 즐겨찾기 토글
	@PatchMapping("/favorite/{id}")
	public ResponseEntity<?> toggleFavorite(Authentication authentication, @PathVariable("id") String id){
		
		MsgFavoriteDTO msgFavoriteDTO = new MsgFavoriteDTO();
		msgFavoriteDTO.setEmpId(((LoginDTO)authentication.getPrincipal()).getUsername());
		msgFavoriteDTO.setFvUser(id);
		
		if (messengerService.searchFavorite(msgFavoriteDTO)) {
			messengerService.deleteFavorite(msgFavoriteDTO);
		} else {
			messengerService.createFavorite(msgFavoriteDTO);
		}
		
		return ResponseEntity.ok().body("success");
	}
	
	// ==========================================================================
	// 메신저 팝업 채팅방 - 대상 선택 진입
	@GetMapping("/target/{id}")
	public String startChat(Authentication authentication, Model model, @PathVariable("id") String id){

		log.info("startChat 진입.......");
		log.info("id..................." + id);

		// target과의 방 찾기
		Long roomId = messengerService.searchRoom(authentication.getName(), id);

		// 진짜 기존 방을 발견했다면
		if (roomId != 0)
			return "redirect:/messenger/room/" + roomId;
		
		// 첫 대화인 경우
		model.addAttribute("targetEmpId", id);
		model.addAttribute("groupYn", "N");
		model.addAttribute("targetName", empRepository.findById(id).get().getEmpName());
		//model.addAttribute("targetPos", empRepository.findById(id).get().get());
		log.info(">>>>>>>>>>>>>>> chat controller... : " + roomId);
		return "/messenger/chat";
	}

	// ==========================================================================
	// 메신저 팝업 채팅방 - 방 선택 진입
	@GetMapping("/room/{id}")
	public String openRoom(Authentication authentication, Model model, @PathVariable("id") Long id){

		// 1) 엔티티 리스트 가져오기
		List<MsgMessage> entityList =
				msgMessageRepository.findByRoomId_RoomIdOrderBySentDate(id);

		// 2) DTO 리스트 변환
		List<MsgMessageDTO> dtoList = new ArrayList<>();
		for (MsgMessage msg : entityList) {
			MsgMessageDTO dto = MsgMessageDTO.fromEntity(msg);

			String senderId = msg.getSenderId().getEmpId();

			// MsgStatus에서 프로필 조회
			Integer profile = msgStatusRepository.findById(senderId)
					.orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"))
					.getMsgProfile();

			dto.setSenderId(senderId);
			dto.setSenderName(msg.getSenderId().getEmpName());
			dto.setSenderProfile(profile);

			dtoList.add(dto);
		}
		
		// 3) 방 소속 멤버 조회
	    List<MsgRelation> relationList = msgRelationRepository.findByRoomId_RoomId(id);
	    List<RoomMemberDTO> memberList = new ArrayList<>();
	    for (MsgRelation relation : relationList) {
	        String memberId = relation.getEmpId().getEmpId();  // 사번 뽑기
	        memberList.add(messengerService.buildRoomMember(memberId));
	    }

		// 3) 모델에 담기
		model.addAttribute("roomId", id);
		model.addAttribute("msgList", dtoList);
		//model.addAttribute("targetEmpId", id);
		model.addAttribute("groupYn", "N");
		//model.addAttribute("targetPos", empRepository.findById(id).get().getPosition());
		model.addAttribute("members", memberList);
		return "/messenger/chat";
	}

	// ==========================================================================
	// 메신저 팝업 채팅방 - 새로운 방 생성
	@PostMapping("/chat")
	public ResponseEntity<?> createRoom(Authentication authentication,
										@RequestBody RoomCreateRequest roomCreateRequest) throws IOException{
		String empId = authentication.getName();

		roomCreateRequest.setCreatedUser(empId);
		roomCreateRequest.getMembers().add(empId);
		Long roomId = messengerService.createRoom(roomCreateRequest);
		return ResponseEntity.ok(Map.of("roomId", roomId));
	}

	// ==========================================================================
	// 메신저 팝업 채팅방 - 기존 방에 메시지 전송
	@PostMapping(value = "/chat/{id}",
				 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> sendMessage(Authentication authentication,
										 @PathVariable("id") Long id,
										 @RequestPart("message") MsgMessageDTO msgMessageDTO,
										 @RequestPart(value = "files", required = false) List<MultipartFile> files)
		throws IOException{
		
		msgMessageDTO.setRoomId(id);
		msgMessageDTO.setSenderId(authentication.getName());
		messengerService.sendMessage(msgMessageDTO, files);
		return ResponseEntity.ok().build();
	}
	
	// ==========================================================================
	// 메시지 읽음 처리
	@PatchMapping("/chat/{roomId}")
	public ResponseEntity<?> updateReadMessage(Authentication authentication,
											@PathVariable("roomId") Long roomId,
											@RequestBody ReadUpdateRequest readUpdateRequest){
	
	String empId = authentication.getName();
	messengerService.updateLastRead(empId, roomId, readUpdateRequest.getLastReadId());
	
	return ResponseEntity.ok().build();
	
	}
	
	// ==========================================================================
	// 채팅방 퇴장 처리
	@DeleteMapping("/room/{roomId}/member")
	public ResponseEntity<?> exitRoom (Authentication authentication, @PathVariable("roomId") Long roomId){
		messengerService.exitRoom(roomId, authentication.getName());
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>> deleteMapping 진입...............");
		return ResponseEntity.noContent().build();
	}
	
	// ==========================================================================
	// 채팅방 검색 기능
	@GetMapping("/rooms/search")
	public ResponseEntity<?> searchRooms(@RequestParam("keyword") String keyword) {
	    return ResponseEntity.ok(messengerService.searchRooms(keyword));
	}


	
}
