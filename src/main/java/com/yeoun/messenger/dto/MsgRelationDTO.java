package com.yeoun.messenger.dto;

import com.yeoun.emp.entity.Emp;
import com.yeoun.messenger.entity.MsgMessage;
import com.yeoun.messenger.entity.MsgRoom;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MsgRelationDTO {

    private long relationId;            // 관계 고유 ID
    private MsgRoom roomId;             // 채팅방 ID
    private Emp empId;                  // 참여자 ID
    private MsgMessage lastReadId;      // 마지막 조회 메시지 ID
    private String pinnedYn;            // 채팅방 고정 여부
    private String participantYn;       // 채팅방 참여 상태
    private LocalDateTime joinDate;     // 마지막 입장 일시
    private LocalDateTime activeDate;   // 마지막 확인 시간
    private LocalDateTime pinnedDate;   // 채팅방 고정 시간
    private String remark;              // 비고



}
