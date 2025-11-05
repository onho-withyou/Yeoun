//package com.yeoun.pay.entity;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EntityListeners;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Lob;
//import jakarta.persistence.SequenceGenerator;
//import jakarta.persistence.Table;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.ToString;
//
///**
// * 급여 기준 정보 (PAY_RULE)
// * - 기본급/식대/교통비/보험요율/지급일/상태/비고 등 일괄 기준 관리
// */
//@Entity
//@Table(name = "PAY_RULE")
//@NoArgsConstructor
//@Getter
//@Setter
//@ToString
//@EntityListeners(AuditingEntityListener.class)
//@SequenceGenerator(
//        name = "PAY_RULE_SEQ_GENERATOR",   // JPA에서 사용할 시퀀스 이름
//        sequenceName = "PAY_RULE_SEQ",     // Oracle 실제 시퀀스 이름
//        initialValue = 1,
//        allocationSize = 1
//)
//public class PayRule {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO, generator = "PAY_RULE_SEQ_GENERATOR")
//    @Column(name = "RULE_ID", nullable = false, updatable = false, precision = 18, scale = 0)
//    private Long ruleId; // 기준 ID (PK)
//
//    @Column(name = "START_DATE", nullable = false)
//    private LocalDate startDt; // 적용시작일
//
//    @Column(name = "END_DATE")
//    private LocalDate endDt;   // 적용종료일 (NULL=무기한)
//
//    // 금액 15,2
//    @Column(name = "BASE_AMT", precision = 15, scale = 2)
//    private Double baseAmt;    // 기본급
//
//    @Column(name = "MEAL_AMT", precision = 15, scale = 2)
//    private Double mealAmt;    // 식대
//
//    @Column(name = "TRANS_AMT", precision = 15, scale = 2)
//    private Double transAmt;   // 교통비
//
//    // 요율 7,4  (예: 0.045 = 4.5%)
//    @Column(name = "PEN_RATE", precision = 7, scale = 4)
//    private Double penRate;    // 국민연금 요율
//
//    @Column(name = "HLTH_RATE", precision = 7, scale = 4)
//    private Double hlthRate;   // 건강보험 요율
//
//    @Column(name = "EMP_RATE", precision = 7, scale = 4)
//    private Double empRate;    // 고용보험 요율
//
//    @Column(name = "TAX_RATE", precision = 7, scale = 4)
//    private Double taxRate;    // 소득세율(간이세율 범위 사용 가능)
//
//    @Column(name = "PAY_DAY", precision = 2, scale = 0)
//    private Integer payDay;    // 지급일(예: 25)
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "STATUS", length = 10, nullable = false)
//    private PayRuleStatus status; // ACTIVE / INACTIVE
//
//    @Lob
//    @Column(name = "REMARK")
//    private String remark;     // 비고(CLOB)
//
//    // 감사 필드
//    @CreatedDate
//    @Column(name = "CREATED_DATE", updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(name = "UPDATED_DATE")
//    private LocalDateTime updatedAt;
//
//    // 빌더 (필수값 위주)
//    @Builder
//    public PayRule(LocalDate startDt,
//                   LocalDate endDt,
//                   Double baseAmt,
//                   Double mealAmt,
//                   Double transAmt,
//                   Double penRate,
//                   Double hlthRate,
//                   Double empRate,
//                   Double taxRate,
//                   Integer payDay,
//                   PayRuleStatus status,
//                   String remark) {
//        this.startDt = startDt;
//        this.endDt = endDt;
//        this.baseAmt = baseAmt;
//        this.mealAmt = mealAmt;
//        this.transAmt = transAmt;
//        this.penRate = penRate;
//        this.hlthRate = hlthRate;
//        this.empRate = empRate;
//        this.taxRate = taxRate;
//        this.payDay = payDay;
//        this.status = status;
//        this.remark = remark;
//    }
//}
