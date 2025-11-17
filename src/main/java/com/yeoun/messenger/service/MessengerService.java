package com.yeoun.messenger.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.yeoun.messenger.dto.*;
import com.yeoun.messenger.entity.MsgMessage;
import com.yeoun.messenger.entity.MsgRelation;
import com.yeoun.messenger.entity.MsgRoom;
import com.yeoun.messenger.repository.MsgMessageRepository;
import com.yeoun.messenger.repository.MsgRelationRepository;
import com.yeoun.messenger.repository.MsgRoomRepository;
import org.springframework.stereotype.Service;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
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
	private final MsgFavoriteRepository msgFavoriteRepository;
	private final EmpRepository empRepository;
	private final MsgRelationRepository msgRelationRepository;
	private final MsgRoomRepository msgRoomRepository;
	private final MsgMessageRepository msgMessageRepository;

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
	public List<MsgRoomDTO> getChatRooms (String username) {
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
	public void sendMessage(MsgMessageDTO msgMessageDTO) {
		MsgMessage msgMessage = msgMessageDTO.toEntity(							// 엔티티 변환
				msgRoomRepository.getReferenceById(msgMessageDTO.getRoomId()),	// roomId
				empRepository.getReferenceById(msgMessageDTO.getSenderId())		// senderId
		);
		msgMessageRepository.save(msgMessage);
	}

	// ========================================================
	// 새 방 생성 & 메시지 보내기
	@Transactional
	public Long createRoom(RoomCreateRequestDTO roomCreateRequestDTO) {

		// 1) 채팅방 생성
		MsgRoom newRoom = new MsgRoom();
		newRoom.setGroupYn(roomCreateRequestDTO.getMembers().size() > 2 ? "Y" : "N");
		newRoom.setGroupName(roomCreateRequestDTO.getGroupName());
		msgRoomRepository.save(newRoom);

		// 2) 참여자 relations 저장
		for (String empId : roomCreateRequestDTO.getMembers()) {
			MsgRelation relation = new MsgRelation();
			relation.setRoomId(newRoom);
			relation.setEmpId(empRepository.getReferenceById(empId));
			msgRelationRepository.save(relation);
		}

		// 3) 첫 메시지 저장
		if (roomCreateRequestDTO.getFirstMessage() != null &&
				!roomCreateRequestDTO.getFirstMessage().isBlank()) {

			MsgMessageDTO msgMessageDTO = new MsgMessageDTO();
			msgMessageDTO.setRoomId(newRoom.getRoomId());
			msgMessageDTO.setSenderId(roomCreateRequestDTO.getCreatedUser());
			msgMessageDTO.setMsgContent(roomCreateRequestDTO.getFirstMessage());
			msgMessageDTO.setMsgType(roomCreateRequestDTO.getMsgType());

			sendMessage(msgMessageDTO);
		}


		return newRoom.getRoomId();

	}

}
