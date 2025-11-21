package com.yeoun.messenger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yeoun.messenger.entity.MsgRoom;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
// 방 목록 조회만을 위한 DTO
public class MsgRoomListDTO {

    private Long roomId;                    // 방 ID
    private String groupName;               // 채팅방 이름
    private String groupYn;                 // 그룹채팅 여부
    private String previewMessage;          // 미리보기 메시지
    private String highlight;               // 하이라이트(검색어)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:ss")
    private LocalDateTime previewTime;      // 미리보기 메시지의 전송 시간
    private Integer unreadCount;            // 읽지 않은 메시지
    private Integer profileImg;             // 프로필 사진
    private String pinnedYn;                // 고정 여부

    // ===========================================================
    public static MsgRoomListDTO from(
            MsgRoom room, String preview, String highlight, LocalDateTime previewTime, int unreadCount, int profileImg, String pinnedYn) {

        return MsgRoomListDTO.builder()
                .roomId(room.getRoomId())
                .groupName(room.getGroupName())
                .groupYn(room.getGroupYn())
                .previewMessage(preview)
                .highlight(highlight)
                .previewTime(previewTime)
                .unreadCount(unreadCount)
                .profileImg(profileImg)
                .pinnedYn(pinnedYn)
                .build();
    }

}
