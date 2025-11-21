package com.yeoun.messenger.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.yeoun.messenger.dto.MsgReadRequest;
import com.yeoun.messenger.dto.MsgSendRequest;
import com.yeoun.messenger.dto.RoomLeaveRequest;
import com.yeoun.messenger.dto.StatusChangeRequest;
import com.yeoun.messenger.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ChatController {
	
	private final ChatService chatService;
	
	
	// ====================================================
	// 클라이언트로부터 수신되는 웹소켓 채팅 메세지(/topic/xxx)를 전달받아 처리할 매핑 작업
	// => 클라이언트에서 서버측으로 "/topic/chat/send" 주소로 전송할 경우
	//	  컨트롤러 측에서 @MessageMapping("xxx") 형태로 매핑 메서드 작성
	// 	  이 때, 경로 변수처럼 바인딩이 필요할 때 @PathVariable 대신 @DestinationVariable 어노테이션 사용(방법은 동일)
	// => 수신된 메세지를 다시 다른 클라이언트들에게 전송할 경우 @SendTo 어노테이션 활용
	
	
	// ====================================================
	// 1. 메시지 전송 
	// 클라이언트 -> pub/chat/send
	@MessageMapping("/chat/send")
	public void sendMessage (MsgSendRequest msgSendRequest) {
		chatService.sendMessage(msgSendRequest);
	}
	
	// ====================================================
	// 2. 메시지 읽음
	// 클라이언트 -> pub/chat/read
	@MessageMapping("/chat/read")
	public void readMessage(MsgReadRequest msgReadRequest) {
		chatService.readMessage(msgReadRequest);
	}
	
	// ====================================================
	// 3. 상태 변경 처리
	// 클라이언트 -> pub/status/change
	@MessageMapping("/status/change")
	public void changeStatus(StatusChangeRequest statusChangeRequest) {
		chatService.changeStatus(statusChangeRequest);
	}
	
	// ====================================================
	// 4. 방 퇴장 알림
	@MessageMapping("/room/leave")
	public void leaveRoom(RoomLeaveRequest roomLeaveRequest) {
		chatService.leaveRoom(roomLeaveRequest);
	}
	

}






