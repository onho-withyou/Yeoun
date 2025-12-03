package com.yeoun.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SHIPMENT_LOT")
public class ShipmentLot {

    // 1) 출하 LOT ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHIPMENT_LOT_ID", nullable = false)
    @Comment("출하 LOT 상세 PK (시스템 자동번호)")
    private Long shipmentLotId;

    // 2) 출하 ID (FK → SHIPMENT.SHIPMENT_ID)
    @Column(name = "SHIPMENT_ID", nullable = false)
    @Comment("어떤 출하 건인지 연결 (출하 이력 FK)")
    private Long shipmentId;

    // 3) LOT 번호
    @Column(name = "LOT_NO", length = 50, nullable = false)
    @Comment("사용된 LOT 식별 번호 (생산/입고 LOT)")
    private String lotNo;

    // 4) 출하수량
    @Column(name = "LOT_QTY", precision = 18, scale = 2, nullable = false)
    @Comment("해당 LOT으로 출하된 수량 (제고 차감 기준)")
    private BigDecimal lotQty;

    // 5) 생성일시
    @Column(name = "CREATED_AT")
    @Comment("등록일시 (시스템 자동기록)")
    private LocalDateTime createdAt;

    // 6) 수정일시
    @Column(name = "UPDATED_AT")
    @Comment("수정일시 (시스템 자동기록)")
    private LocalDateTime updatedAt;


    // =============================
    // 기본값 처리
    // =============================
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
