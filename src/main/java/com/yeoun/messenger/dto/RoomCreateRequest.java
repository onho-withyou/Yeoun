package com.yeoun.messenger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RoomCreateRequest {
    private Set<String> members;         // 참여 멤버
    private String createdUser;          // 생성자 EMP_ID
    private String groupName;            // 그룹 채팅방 이름 (1:1이면 null)
    private String firstMessage;         // 첫번째 메시지
    private String msgType;              // 메시지 타입
}
