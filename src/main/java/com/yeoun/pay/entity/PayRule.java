package com.yeoun.pay.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;  
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.pay.enums.ActiveStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PAY_RULE")
@NoArgsConstructor
@Getter @Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "PAY_RULE_SEQ_GENERATOR",
        sequenceName = "PAY_RULE_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class PayRule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAY_RULE_SEQ_GENERATOR")
    @Column(name = "RULE_ID", nullable = false, updatable = false)
    @Comment("기준 ID (PK)")
    private Long ruleId;

    @Column(name = "START_DATE", nullable = false)
    @Comment("적용 시작일 (해당 급여 기준이 적용되기 시작하는 날짜)")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    @Comment("적용 종료일 (NULL=무기한)")
    private LocalDate endDate;

    // 금액: NUMBER(15,2) — Double 사용 시 columnDefinition 지정
    @Column(name = "BASE_AMT", columnDefinition = "NUMBER(15,2)")
    @ColumnDefault("0")
    @Comment("기본금: 사원 급여 산정 시 기준금액 또는 직급별 표준값")
    private Double baseAmt;

    @Column(name = "MEAL_AMT", columnDefinition = "NUMBER(15,2)")
    @ColumnDefault("0")
    @Comment("식대: 별도 입력 없을 시 적용하는 참조 금액")
    private Double mealAmt;

    @Column(name = "TRANS_AMT", columnDefinition = "NUMBER(15,2)")
    @ColumnDefault("0")
    @Comment("교통비: 기본값(정액). 별도 입력 없을 때 사용")
    private Double transAmt;

    // 요율: NUMBER(7,4)
    @Column(name = "PEN_RATE", columnDefinition = "NUMBER(7,4)")
    @ColumnDefault("0.045")
    @Comment("국민연금 요율 (회사/개인 합산 또는 정책상 기준치) 예: 0.045 = 4.5%")
    private Double penRate;

    @Column(name = "HLTH_RATE", columnDefinition = "NUMBER(7,4)")
    @ColumnDefault("0.035")
    @Comment("건강보험 요율 예: 0.035 = 3.5%")
    private Double hlthRate;

    @Column(name = "EMP_RATE", columnDefinition = "NUMBER(7,4)")
    @ColumnDefault("0.009")
    @Comment("고용보험 요율 예: 0.009 = 0.9%")
    private Double empRate;

    @Column(name = "TAX_RATE", columnDefinition = "NUMBER(7,4)")
    @ColumnDefault("0.05")
    @Comment("소득세율(예: 간이세율 또는 평균세율 참조) 예: 0.05 = 5%")
    private Double taxRate;

    @Column(name = "PAY_DAY")
    @ColumnDefault("25")
    @Comment("지급일자(매월 급여지급 기준일, 1~31)")
    private Integer payDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    @ColumnDefault("'ACTIVE'")
    @Comment("상태: ACTIVE(현재 사용 중) / INACTIVE(비활성화)")
    private ActiveStatus status;

    @Lob
    @Column(name = "REMARK")
    @Comment("비고(관리자 메모/설명)")
    private String remark;

    @CreatedDate
    @Column(name = "CREATED_DATE", updatable = false)
    @Comment("생성일시")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "UPDATED_DATE")
    @Comment("수정일시")
    private LocalDateTime updatedDate;

    @Builder
    public PayRule(LocalDate startDate,
                   LocalDate endDate,
                   Double baseAmt,
                   Double mealAmt,
                   Double transAmt,
                   Double penRate,
                   Double hlthRate,
                   Double empRate,
                   Double taxRate,
                   Integer payDay,
                   ActiveStatus status,
                   String remark) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.baseAmt = baseAmt;
        this.mealAmt = mealAmt;
        this.transAmt = transAmt;
        this.penRate = penRate;
        this.hlthRate = hlthRate;
        this.empRate = empRate;
        this.taxRate = taxRate;
        this.payDay = payDay;
        this.status = status;
        this.remark = remark;
    }

    /** 널로 들어오는 경우를 대비한 엔티티 레벨 기본값 보정 */
    @PrePersist
    public void applyDefaults() {
        if (baseAmt == null) baseAmt = 0d;
        if (mealAmt == null) mealAmt = 0d;
        if (transAmt == null) transAmt = 0d;
        if (penRate == null) penRate = 0.045d;
        if (hlthRate == null) hlthRate = 0.035d;
        if (empRate == null) empRate = 0.009d;
        if (taxRate == null) taxRate = 0.05d;
        if (payDay == null) payDay = 25;
        if (status == null) status = ActiveStatus.ACTIVE;
    }
}
