package com.yeoun.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ORDERS")
public class Orders {

    // 1) 수주ID (PK)
    @Id
    @Column(name = "ORDER_ID", length = 30, nullable = false)
    @Comment("수주(주문) 건을 식별하기 위한 고유 ID (ORD + YYYYMMDD + 4자리 Sequence)")
    private String orderId;

    // 2) 거래처 ID (FK)
    @Column(name = "CLIENT_ID", length = 20, nullable = false)
    @Comment("주문을 요청한 거래처 ID")
    private String clientId;

    // 3) 담당자 ID (내부 직원, FK)
    @Column(name = "EMP_ID", length = 20, nullable = false)
    @Comment("수주를 등록/요청받은 내부 담당자 ID")
    private String empId;

    // 4) 주문번호 (외부 주문번호)
    @Column(name = "ORDER_NUM", length = 30)
    @Comment("외부에서 제공되는 주문 번호")
    private String orderNum;

    // 5) 수주일자
    @Column(name = "ORDER_DATE")
    @Comment("수주가 시스템에 등록된 일자")
    private LocalDate orderDate;

    // 6) 납기일자(고객 요청일자)
    @Column(name = "DELIVERY_DATE")
    @Comment("고객이 요청한 납기일")
    private LocalDate deliveryDate;

    // 7) 수주상태
    @Column(name = "ORDER_STATUS", length = 20, nullable = false)
    @Comment("수주 처리 상태 (요청/진행/완료/취소 등)")
    private String orderStatus;

    // 8) 배송지 우편번호
    @Column(name = "D_POSTCODE", length = 20)
    @Comment("배송지 우편번호")
    private String deliveryPostcode;

    // 9) 배송지 기본주소
    @Column(name = "D_ADDRESS1", length = 255)
    @Comment("배송지 기본 주소")
    private String deliveryAddress1;

    // 10) 배송지 상세주소
    @Column(name = "D_ADDRESS2", length = 255)
    @Comment("배송지 상세 주소")
    private String deliveryAddress2;

    // 11) 주문메모
    @Column(name = "ORDER_MEMO", length = 200)
    @Comment("주문에 대한 요청 사항 및 메모")
    private String orderMemo;

    // 12) 등록일시
    @Column(name = "CREATED_AT")
    @Comment("데이터 생성 일시")
    private LocalDateTime createdAt;

    // 13) 등록자 ID (EMP FK)
    @Column(name = "CREATED_BY", length = 20)
    @Comment("최초 데이터 입력자")
    private String createdBy;


    // =============================
    // 기본값 자동 처리
    // =============================
    @PrePersist
    public void prePersist() {
        if (this.orderStatus == null) this.orderStatus = "REQUEST"; // 기본 상태
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.orderDate == null) this.orderDate = LocalDate.now();
    }
}
