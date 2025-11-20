package com.yeoun.messenger.dto;

import com.yeoun.emp.entity.Emp;
import com.yeoun.messenger.entity.MsgMessage;
import com.yeoun.messenger.entity.MsgRelation;
import com.yeoun.messenger.entity.MsgRoom;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.modelmapper.ModelMapper;
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

    // ===========================================================
    // DTO <-> Entity 변환 메서드 구현

    private static ModelMapper modelMapper = new ModelMapper();

    public MsgRoom toEntity(Emp empId) {
        MsgRoom msgRoom = new MsgRoom();
        msgRoom.setUpdatedUser(empId);
        return msgRoom;
    }

    public static MsgRoomDTO fromEntity(MsgRoom msgRoom) {
        MsgRoomDTO dto = new MsgRoomDTO();

        dto.setRoomId(msgRoom.getRoomId());
        dto.setGroupName(msgRoom.getGroupName());
        dto.setGroupYn(msgRoom.getGroupYn());
        dto.setUpdatedDate(msgRoom.getUpdatedDate());

        // updatedUser
        if (msgRoom.getUpdatedUser() != null) {
            dto.setUpdatedUser(msgRoom.getUpdatedUser());
        }

        return dto;
    }

}





