package com.yeoun.pay.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.enums.CalcType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PAY_RUN")
@SequenceGenerator(
        name = "SEQ_PAY_RUN_GEN",
        sequenceName = "SEQ_PAY_RUN",
        allocationSize = 1
)
public class PayRun {

    // 작업 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PAY_RUN_GEN")
    @Column(name = "RUN_ID", nullable = false, columnDefinition = "NUMBER(18)")
    @Comment("작업 ID (PK) — 급여 계산 실행(배치 또는 개별)의 고유 식별자")
    private Long runId;

    // 급여월 (YYYYMM)
    @Column(name = "PAY_YMMM", nullable = false, columnDefinition = "CHAR(6)")
    @Comment("급여월 (YYYYMM 형식) — 급여 계산 대상 월")  
    private String payYmmm;

    // 계산유형 (BATCH_ALL / SINGLE / REPROD)
    @Enumerated(EnumType.STRING)
    @Column(name = "CALC_TYPE", length = 20, nullable = false)
    @Comment("계산 유형: BATCH_ALL(전체), SINGLE(단일 사원), REPROD(재계산)")
    private CalcType calcType;

    // 상태 (SIMULATED / CALCULATED / CONFIRMED / CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    @Comment("계산 상태: SIMULATED(시뮬레이션), CALCULATED(계산 완료), CONFIRMED(확정), CANCELLED(취소)")
    private CalcStatus status;

    // 요청자 (EMPLOYEE FK 예정)
    @Column(name = "REQ_USER", columnDefinition = "NUMBER(18)")
    @Comment("요청자 ID — 급여 계산을 요청한 관리자 (EMPLOYEE 테이블 FK 예정)")
    private Long reqUser;

    // 시작시간
    @Column(name = "STARTED_DATE", columnDefinition = "TIMESTAMP")
    @Comment("급여 계산 시작 시각 (자동 기록)")
    private LocalDateTime startedDate;

    // 종료시간
    @Column(name = "ENDED_DATE", columnDefinition = "TIMESTAMP")
    @Comment("급여 계산 종료 시각 (자동 기록)")
    private LocalDateTime endedDate;

    // 비고
    @Column(name = "REMARK", length = 400)
    @Comment("비고 — 실행 로그, 오류 메모, 추가 설명 등")
    private String remark;

    @PrePersist
    public void prePersist() {
        if (this.calcType == null) this.calcType = CalcType.BATCH_ALL;
        if (this.status == null) this.status =CalcStatus.SIMULATED;
        if (this.startedDate == null) this.startedDate = LocalDateTime.now();
    }
}
