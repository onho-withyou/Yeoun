package com.yeoun.messenger.entity;

import com.yeoun.emp.entity.Emp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "MSG_FAVORITE")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class MsgFavorite {

    // 고유 ID (EMP_ID & FV_USER)
    @EmbeddedId
    private MsgFavoriteId id;

    // EMP_ID
    @MapsId("empId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", nullable = false)
    private Emp empId;

    // FV_USER
    @MapsId("fvUser")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FV_USER", nullable = false)
    private Emp fvUser;

    // 추가 일시
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdDate;

}