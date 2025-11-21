package com.yeoun.messenger.entity;

import com.yeoun.emp.entity.Emp;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MSG_ROOM")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
public class MsgRoom {

    // 채팅방 ID
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ROOM_SEQ")
    @SequenceGenerator(
            name = "ROOM_SEQ",
            sequenceName = "ROOM_SEQ",
            allocationSize = 1
    )
    private long roomId;

    // 그룹채팅 여부
    @Column(nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String groupYn;

    // 그룹 채팅방 이름
    @Column(length = 50)
    private String groupName;

    // 생성 일시
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdDate;

    // 이름 변경 일시
    @LastModifiedDate
    @Column
    private LocalDateTime updatedDate;

    // 변경 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPDATED_USER")
    private Emp updatedUser;

    // ====================================================
    // 연관관계 매핑

    @OneToMany(mappedBy = "roomId")
    private List<MsgRelation> relations = new ArrayList<>();

    @OneToMany(mappedBy = "roomId")
    private List<MsgMessage> messages = new ArrayList<>();

    // 필요한 최소 생성자 — ID만 있는 방(프록시) 만들 때 사용
    @Builder(builderMethodName = "IdOnly")
    public static MsgRoom createById(Long roomId) {
        MsgRoom room = new MsgRoom();
        room.setRoomId(roomId);
        return room;
    }

}





