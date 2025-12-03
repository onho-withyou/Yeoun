package com.yeoun.production.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTION_PLAN")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductionPlan {

    @Id
    @Column(name = "PLAN_ID", length = 20)
    @Comment("생산계획 고유식별자 (PLAN + YYYYMMDD + 시퀀스)")
    private String planId;

    @Column(name = "ORDER_ID", length = 20, nullable = false)
    @Comment("수주(ORDER) ID")
    private String orderId;

    @Column(name = "PLAN_DATE", nullable = false)
    @Comment("계획일자")
    private LocalDate planDate;

    @Column(name = "DUE_DATE", nullable = false)
    @Comment("납기일자")
    private LocalDate dueDate;

    @Column(name = "STATUS", length = 20, nullable = false)
    @Comment("계획 상태 (PLANNING / CONFIRMED)")
    private String status;

    @Column(name = "PLAN_MEMO", length = 1000)
    @Comment("계획 메모")
    private String planMemo;

    @CreatedDate
    @Column(name = "CREATED_AT")
    @Comment("생성 시각")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY", length = 30)
    @Comment("생성자 ID")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    @Comment("수정 시각")
    private LocalDateTime updatedAt;
}
