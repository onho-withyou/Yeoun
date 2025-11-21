package com.yeoun.messenger.service;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.messenger.dto.MsgMessageDTO;
import com.yeoun.messenger.dto.MsgReadRequest;
import com.yeoun.messenger.dto.MsgSendRequest;
import com.yeoun.messenger.dto.RoomLeaveRequest;
import com.yeoun.messenger.dto.StatusChangeRequest;
import com.yeoun.messenger.entity.MsgMessage;
import com.yeoun.messenger.entity.MsgRoom;
import com.yeoun.messenger.entity.MsgStatus;
import com.yeoun.messenger.repository.MsgMessageRepository;
import com.yeoun.messenger.repository.MsgRoomRepository;
import com.yeoun.messenger.repository.MsgStatusRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatService {
	
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final MsgMessageRepository msgMessageRepository;
	private final MessengerService messengerService;
	private final EmpRepository empRepository;
	private final MsgRoomRepository msgRoomRepository;
	private final MsgStatusRepository msgStatusRepository;
	
	// 1) 메시지 전송 처리
	// DB 저장 + 해당 방 참여자들에게 broadcast
	@Transactional
	public void sendMessage(MsgSendRequest msgSendRequest) {
		

	    // 1) 엔티티 조회 (FK 정상 매핑)
	    Emp sender = empRepository.findById(msgSendRequest.getSenderId())
	            .orElseThrow(() -> new RuntimeException("해당 유저가 없음!"));

	    MsgRoom room = msgRoomRepository.findById(msgSendRequest.getRoomId())
	            .orElseThrow(() -> new RuntimeException("해당 방이 없음!"));
	    
	    MsgStatus status = msgStatusRepository.findById(msgSendRequest.getSenderId())
	    		.orElseThrow(() -> new RuntimeException("해당 유저 프로필이 없음!"));
		
		// DB 저장
		MsgMessage saved = msgMessageRepository.save(
				   MsgMessage.builder()
				             .roomId(msgSendRequest.getRoomId())
				             .senderId(msgSendRequest.getSenderId())
				             .msgContent(msgSendRequest.getMessage())
				             .msgType("TEXT")
				             .build()
		 );
		saved.setSentDate(LocalDateTime.now());
		 
		MsgMessageDTO dto = MsgMessageDTO.fromEntity(saved);
		dto.setSenderName(sender.getEmpName());
		dto.setSentDate(saved.getSentDate());
		dto.setSenderProfile(status.getMsgProfile());
		
		 // 메시지 전송
		 simpMessagingTemplate.convertAndSend(
				   "/topic/chat/room/" + msgSendRequest.getRoomId(),
				   dto
		 );
		 
		log.info("************** STOMP! 메시지 전송" + dto);
	}
	
	
	// 2) 메시지 읽음 처리
	public void readMessage(MsgReadRequest msgReadRequest) {
		
		// DB 저장
		messengerService.updateLastRead(
				msgReadRequest.getReaderId(),
				msgReadRequest.getRoomId(), 
				msgReadRequest.getMsgId()
		);
		
		// 메시지 전송
		simpMessagingTemplate.convertAndSend(
				"/topic/chat/room" + msgReadRequest.getRoomId() + "/read",
				msgReadRequest
		);
		
		log.info("************** STOMP! 메시지 읽음" + msgReadRequest);
	}
	
	
	// 3) 상태 변경 처리
	public void changeStatus(StatusChangeRequest statusChangeRequest) {
		
		// DB 저장 =====> 메모리 or Redis 변환 예정
		messengerService.updateStatus(statusChangeRequest);
		
		// 메시지 전송
		simpMessagingTemplate.convertAndSend(
				"/topic/status",
				statusChangeRequest);
		
		log.info("************** STOMP! 상태 변경" + statusChangeRequest);
	}
	
	// 4) 방 퇴장 알림
	public void leaveRoom(RoomLeaveRequest roomLeaveRequest) {
		
		// DB 저장
		messengerService.exitRoom(roomLeaveRequest.getRoomId(), roomLeaveRequest.getEmpId());
	
		// 메시지 전송
		simpMessagingTemplate.convertAndSend(
				"/topic/chat/" + roomLeaveRequest.getRoomId() + "/leave", 
				roomLeaveRequest
		);
		
		log.info("************** STOMP! 방 퇴장" + roomLeaveRequest);
	}
	

}







