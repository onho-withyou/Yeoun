package com.yeoun.pay.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Dept;
import com.yeoun.pay.entity.PayrollPayslip;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EMP_PAY")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class EmpPay {

    // ============================
    // PK
    // ============================
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EMP_PAY")
    @SequenceGenerator(
            name = "SEQ_EMP_PAY",
            sequenceName = "SEQ_EMP_PAY_ID",
            allocationSize = 1
    )
    @Column(name = "EMP_PAY_ID")
    @Comment("사원 급여 결과 고유 ID")
    private Long empPayId;


    // ============================
    // FK: PAYROLL_PAYSLIP
    // ============================
    @Column(name = "PAYSLIP_ID", nullable = false)
    @Comment("급여명세서 ID")
    private Long payslipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAYSLIP_ID", insertable = false, updatable = false)
    private PayrollPayslip payslip;


    // ============================
    // FK: EMP
    // ============================
    @Column(name = "EMP_ID", nullable = false, length = 20)
    @Comment("사원 ID")
    private String empId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", insertable = false, updatable = false)
    private Emp emp;


    // ============================
    // FK: DEPT
    // ============================
    @Column(name = "DEPT_ID", nullable = false)
    @Comment("부서 ID")
    private String deptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPT_ID", insertable = false, updatable = false)
    private Dept dept;


    // ============================
    // 금액 관련
    // ============================
    @Column(name = "GROSS_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("총지급액 (기본급 + 수당 합계)")
    private BigDecimal grossAmt = BigDecimal.ZERO;

    @Column(name = "DEDUCTION_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("총공제액")
    private BigDecimal deductionAmt = BigDecimal.ZERO;

    @Column(name = "NET_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("실수령액 (총지급액 - 총공제액)")
    private BigDecimal netAmt = BigDecimal.ZERO;


    // ============================
    // 계좌 정보
    // ============================
    @Column(name = "BANK_CODE", length = 20)
    @Comment("지급 계좌 은행 코드")
    private String bankCode;

    @Column(name = "BANK_ACCT_NO", length = 64)
    @Comment("지급 계좌번호")
    private String bankAcctNo;


    // ============================
    // 지급일시
    // ============================
    @Column(name = "PAY_DATE")
    @Comment("실제 지급 일시")
    private LocalDateTime payDate;


    // ============================
    // 엑셀 연동 키
    // ============================
    @Column(name = "EXP_KEY", length = 36)
    @Comment("전표/리포트 엑셀 연동 키")
    private String expKey;


    // ============================
    // 비고
    // ============================
    @Column(name = "REMARK", length = 200)
    @Comment("비고 및 메모")
    private String remark;
}
