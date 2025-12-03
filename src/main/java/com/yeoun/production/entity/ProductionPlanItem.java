package com.yeoun.production.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTION_PLAN_ITEM")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductionPlanItem {

    @Id
    @Column(name = "PLAN_ITEM_ID", length = 20)
    @Comment("생산계획 상세ID (PLAN_ITEM + 시퀀스)")
    private String planItemId;

    @Column(name = "PLAN_ID", length = 20, nullable = false)
    @Comment("생산계획 마스터ID")
    private String planId;

    @Column(name = "PRD_ID", length = 20, nullable = false)
    @Comment("제품 ID")
    private String prdId;

    @Column(name = "ORDER_QTY", precision = 10, scale = 2, nullable = false)
    @Comment("수주된 수량")
    private BigDecimal orderQty;

    @Column(name = "PLAN_QTY", precision = 10, scale = 2, nullable = false)
    @Comment("계획 생산수량")
    private BigDecimal planQty;

    @Column(name = "BOM_READY_YN", length = 1, nullable = false)
    @Comment("BOM 작성 여부")
    private String bomReadyYn;

    @Column(name = "ITEM_MEMO", length = 300)
    @Comment("상세 메모")
    private String itemMemo;

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
