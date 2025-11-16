package com.yeoun.messenger.dto;

import com.yeoun.emp.entity.Emp;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MsgRoomDTO {

    private long roomId;                // 방 ID
    private String groupYn;             // 그룹채팅 여부
    private String groupName;           // 그룹 채팅방 이름
    private LocalDateTime createdDate;  // 생성 일시
    private LocalDateTime updatedDate;  // 이름 변경 일시
    private Emp updatedUser;            // 변경 사용자

    private String pinnedYn;            // 해당 방 고정 여부 (추가 필드)
    private Integer profileImg;         // 해당 방에 사용될 프로필 이미지
    private String lastMessage;         // 마지막 메시지 (추가 필드)
    private LocalDateTime lastMessageTime; // 마지막 메시지가 전송된 시간 (추가 필드)
    private int unreadCount;            // 읽지 않은 메시지 수 (추가 필드)

}





