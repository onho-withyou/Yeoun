package com.yeoun.messenger.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import com.yeoun.messenger.dto.*;
import com.yeoun.messenger.entity.MsgMessage;
import com.yeoun.messenger.entity.MsgRelation;
import com.yeoun.messenger.entity.MsgRoom;
import com.yeoun.messenger.entity.MsgStatus;
import com.yeoun.messenger.repository.MsgMessageRepository;
import com.yeoun.messenger.repository.MsgRelationRepository;
import com.yeoun.messenger.repository.MsgRoomRepository;
import com.yeoun.messenger.repository.MsgStatusRepository;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.common.util.FileUtil;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Position;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.messenger.entity.MsgFavorite;
import com.yeoun.messenger.mapper.MessengerMapper;
import com.yeoun.messenger.repository.MsgFavoriteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class MessengerService {
	
	private final MessengerMapper messengerMapper;
	private final MsgRoomRepository msgRoomRepository;
	private final MsgStatusRepository msgStatusRepository;
	private final MsgMessageRepository msgMessageRepository;
	private final MsgFavoriteRepository msgFavoriteRepository;
	private final MsgRelationRepository msgRelationRepository;
	
	// messenger 외 repository
	private final FileUtil fileUtil;
	private final EmpRepository empRepository;
	private final DeptRepository deptRepository;
	private final PositionRepository positionRepository;
	private final FileAttachRepository fileAttachRepository;

	// ====================================================
	// 하이라이트 처리 관련 유틸 함수
	private String highlight(String text, String keyword) {
		if (text == null || keyword == null) return text;

		// (?i)는 대소문자 무시
		return text.replaceAll("(?i)" + Pattern.quote(keyword),
				"<mark>$0</mark>");
	}
	
	// ====================================================
	// 친구 목록을 불러오는 서비스
	public List<MsgStatusDTO> getUsers(String username) {

		// 메신저 내 최종 상태를 결정
		List<MsgStatusDTO> dtoList = messengerMapper.selectUsers(username);
		for (int i = 0; i < dtoList.size(); i++) {
			if(dtoList.get(i).getOnlineYn().equals("N")){
				dtoList.get(i).setStatus("OFFLINE");	// 오프라인
			} else if (dtoList.get(i).getAvlbStat().equals("ONLINE")){
				dtoList.get(i).setStatus("ONLINE");		// 온라인
			} else if (dtoList.get(i).getAvlbStat().equals("AWAY")){
				dtoList.get(i).setStatus("AWAY");		// 자리비움
			} else {
				dtoList.get(i).setStatus("BUSY");		// 다른 용무중
			}
		}

		return dtoList;
	}

	// ====================================================
	// 대화 목록을 불러오는 서비스
	public List<MsgRoomListDTO> getChatRooms (String username) {
		return messengerMapper.selectChats(username);
	}

	// ========================================================
	// 즐겨찾기 추가
	public void createFavorite(MsgFavoriteDTO msgFavoriteDTO) {
		
		log.info("★★★★★★★★★★★★★★★★★★★★★★★ 즐겨찾기 추가 진입....");
		
		Emp empId = empRepository.findByEmpId(msgFavoriteDTO.getEmpId())
				.orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다"));
		Emp fvUser = empRepository.findByEmpId(msgFavoriteDTO.getFvUser())
				.orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다"));
		
		MsgFavorite msgFavorite = msgFavoriteDTO.toEntity(empId, fvUser);
		msgFavoriteRepository.save(msgFavorite);
	}

	// ========================================================
	// 즐겨찾기 여부 확인
	public boolean searchFavorite(MsgFavoriteDTO msgFavoriteDTO) {
		log.info("★★★★★★★★★★★★★★★★★★★★★★★ 즐겨찾기 확인 진입....");
		return msgFavoriteRepository.existsByEmpId_EmpIdAndFvUser_EmpId(msgFavoriteDTO.getEmpId(), msgFavoriteDTO.getFvUser());
	}

	// ========================================================
	// 즐겨찾기 삭제
	@Transactional
	public boolean deleteFavorite(MsgFavoriteDTO msgFavoriteDTO) {
		log.info("★★★★★★★★★★★★★★★★★★★★★★★ 즐겨찾기 삭제 진입....");
		return msgFavoriteRepository
				.deleteByEmpId_EmpIdAndFvUser_EmpId(msgFavoriteDTO.getEmpId(), msgFavoriteDTO.getFvUser()) > 0;
	}


	// ========================================================
	// 1:1 방 생성 여부 확인
	public Long searchRoom(String authUser, String targetUser) {
		Long roomId = msgRelationRepository.findOneToOneRoom(authUser, targetUser);
		log.info(">>>>>>>>>>>>>>>>>>>>>> searchRoom... ID : " + roomId);
		return roomId != null ? roomId : 0;
	}

	// ========================================================
	// 메시지 보내기
	@Transactional
	public void sendMessage(MsgMessageDTO msgMessageDTO, List<MultipartFile> files) throws IOException {
		
		MsgRoom msgRoom = msgRoomRepository.getReferenceById(msgMessageDTO.getRoomId());
		Emp sender = empRepository.getReferenceById(msgMessageDTO.getSenderId());
		
		// 1) 메시지 저장
		MsgMessage msgMessage = msgMessageDTO.toEntity(msgRoom, sender);
		MsgMessage savedMessage = msgMessageRepository.save(msgMessage);
		
		// 2) 파일 업로드
		if (files != null && !files.isEmpty()) {
			List<FileAttach> uploaded = fileUtil.uploadFile(savedMessage, files)
					.stream()
					.map(FileAttachDTO::toEntity)
					.toList();
			
			fileAttachRepository.saveAll(uploaded);
		}
		
	}

	// ========================================================
	// 새 방 생성 & 메시지 보내기
	@Transactional
	public Long createRoom(RoomCreateRequest roomCreateRequestDTO) throws IOException {
		
		log.info("roomCreateRequestDTO : " + roomCreateRequestDTO);

		// 1) 채팅방 생성
		MsgRoom newRoom = new MsgRoom();
		newRoom.setGroupYn(roomCreateRequestDTO.getGroupYn());
		newRoom.setGroupName(roomCreateRequestDTO.getGroupName());
		msgRoomRepository.save(newRoom);

		// 2) 참여자 relations 저장
		for (String empId : roomCreateRequestDTO.getMembers()) {
			MsgRelation relation = new MsgRelation();
			relation.setRoomId(newRoom);
			relation.setEmpId(empRepository.getReferenceById(empId));
			msgRelationRepository.save(relation);
		}

		// 3) 첫 메시지가 텍스트인 경우에 저장
		if (roomCreateRequestDTO.getFirstMessage() != null &&
				!roomCreateRequestDTO.getFirstMessage().isBlank()) {

			MsgMessageDTO msgMessageDTO = new MsgMessageDTO();
			msgMessageDTO.setRoomId(newRoom.getRoomId());
			msgMessageDTO.setSenderId(roomCreateRequestDTO.getCreatedUser());
			msgMessageDTO.setMsgContent(roomCreateRequestDTO.getFirstMessage());
			msgMessageDTO.setMsgType("TEXT");
			
			sendMessage(msgMessageDTO, null);
		}

		return newRoom.getRoomId();
	}

	// ========================================================
	// 마지막으로 읽은 메시지 체크
	@Transactional
	public void updateLastRead(String empId, Long roomId, Long lastReadId) {
		log.info("update last read 진입.............");
		log.info("empId / roomId / lastreadId :::: " + empId + "/" + roomId + "/" + lastReadId);
		msgRelationRepository.updateLastRead(empId, roomId, lastReadId);
	}
	
	// ========================================================
	// 방 내 인원 정보 조회
	public RoomMemberDTO buildRoomMember (String empId) {
		Emp emp = empRepository.findById(empId)
				.orElseThrow(() -> new RuntimeException("사용자 없음"));
		
		MsgStatus msgStatus = msgStatusRepository.findById(empId)
				.orElseThrow(() -> new RuntimeException("사용자 프로필 없음"));
		
		String posName = positionRepository.findById(emp.getPosition().getPosCode())
				.map(Position::getPosName).orElse("미정");
				
		String deptName = deptRepository.findById(emp.getDept().getDeptId())
				.map(Dept::getDeptName).orElse("미정");
		
		return RoomMemberDTO.of(emp, msgStatus, posName, deptName);
	}

	// ========================================================
	// 내 상태 실시간 변경
	@Transactional
	public void updateStatus(String name, MsgStatusDTO msgStatusDTO) {
		MsgStatus msgStatus = msgStatusRepository.findById(name)
				.orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
		
		if (msgStatusDTO.getAvlbStat() != null) {
			msgStatus.setAvlbStat(msgStatusDTO.getAvlbStat());
		}
		
		if (msgStatusDTO.getManualWorkStat() != null) {
			msgStatus.setManualWorkStat(msgStatusDTO.getManualWorkStat());
		}
		
		msgStatus.setWorkStatSource("MANUAL");
		msgStatus.setWorkStatUpdated(LocalDateTime.now());
		
	}

	// ========================================================
	// 방에서 나가기 처리
	@Transactional
	public void exitRoom(Long roomId, String empId) {
		MsgRelation relation = msgRelationRepository.findByRoomId_RoomIdAndEmpId_EmpId(roomId, empId)
				.orElseThrow(() -> new RuntimeException("참여자 없음"));
				
		relation.setParticipantYn("N");
	}
	
	// ========================================================
	// 대화방 검색 기능
	public List<MsgRoomListDTO> searchRooms(String empId, String keyword) {
		if (keyword == null || keyword.isBlank())
			return Collections.emptyList();

		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 대화방 검색 진입... = ");
		// 1) roomId 검색
	    List<Long> byName 	 = msgRoomRepository.findRoomIdByGroupNameContaining(keyword);
	    List<Long> byMember  = msgRelationRepository.findRoomIdByMemberName(keyword);
	    List<Long> byMessage = msgMessageRepository.findRoomIdByMessageContent(keyword);

		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> byName = " + byName);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> byMember = " + byMember);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> byMessage = " + byMessage);

	    // 2) 중복 제거 및 모으기
	    Set<Long> roomIds = new HashSet<>();
	    roomIds.addAll(byName);
	    roomIds.addAll(byMember);
	    roomIds.addAll(byMessage);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> roomIds = " + roomIds);

		// 3) 현재 유저가 속한 방만 남기기
		List<Long> myRooms = msgRelationRepository.findRoomIdsByEmpId(empId);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> myRooms = " + myRooms);
		roomIds.retainAll(myRooms);
		if (roomIds.isEmpty())
			return Collections.emptyList();

		// 4) 방 목록 생성
		List<MsgRoomListDTO> result = new ArrayList<>();

		for (Long roomId : roomIds) {

			// a) 기본 방 정보 찾기
			MsgRoomListDTO room = messengerMapper.selectChat(empId, roomId);

			// b) 메시지 내용에서 매칭되는 문장 찾기
			String message = msgMessageRepository.findMatchedMessage(roomId, keyword);

			// 검색어가 있을 경우 해당 메시지를 보여주고 하이라이트 처리
			if (message != null) {
				room.setPreviewMessage(highlight(message, keyword));
			}

			// c) 이름/그룹명에서 매칭되는 결과에 하이라이트 처리
			//String groupName = room.getGroupName();
			//if (groupName != null && !groupName.isBlank()) {
			//	room.setGroupName(highlight(groupName, keyword));
			//}

			result.add(room);

		}
		
		// 4) 최신 순 정렬
		result.sort(Comparator.comparing(MsgRoomListDTO::getPreviewTime).reversed());
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> result = " + result);
		return result;
	}

	
	
	
	

}
