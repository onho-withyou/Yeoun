package com.yeoun.pay.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.pay.enums.CalcStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PAYROLL_PAYSLIP",
       uniqueConstraints = {
         @UniqueConstraint(name="UX_PAYROLL_PAYSLIP_YYMM_EMP",
                           columnNames={"PAY_YYMM","EMP_ID"})
       })
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PayrollPayslip {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PAYROLL_PAYSLIP")
    @SequenceGenerator(name = "SEQ_PAYROLL_PAYSLIP", sequenceName = "SEQ_PAYROLL_PAYSLIP_ID", allocationSize = 1)
    @Column(name = "PAYSLIP_ID")
    private Long payslipId;


    @Column(name = "EMP_ID", nullable = false, length = 20)
    private String empId;
    
    @Transient
    public String getEmpName() {
        return null; 
    }

    @Column(name = "DEPT_ID", length = 20)
    private String deptId;
    
    /* ===== 사원/부서 조인 객체 ===== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "EMP_ID",
            referencedColumnName = "EMP_ID",
            insertable = false,
            updatable = false
    )
    private Emp emp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "DEPT_ID",
            referencedColumnName = "DEPT_ID",
            insertable = false,
            updatable = false
    )
    private Dept dept;    
    

    @Column(name = "PAY_YYMM", nullable = false, length = 6)
    @Comment("급여 기준월YYYYMM")
    private String payYymm; // YYYYMM

    @Column(name = "BASE_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("기본금 금액")
    @Builder.Default private BigDecimal baseAmt = BigDecimal.ZERO;

    @Column(name = "ALW_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("총 수당 금액")
    @Builder.Default private BigDecimal alwAmt = BigDecimal.ZERO;
    
    @Column(name = "INC_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("인센티브 금액")
    @Builder.Default private BigDecimal incAmt = BigDecimal.ZERO;

    @Column(name = "DED_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("총 공제 금액")
    @Builder.Default private BigDecimal dedAmt = BigDecimal.ZERO;

    @Column(name = "TOT_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("총 지급 금액 ")
    @Builder.Default private BigDecimal totAmt = BigDecimal.ZERO;

    @Column(name = "NET_AMT", nullable = false, precision = 15, scale = 2)
    @Comment("실 수령 금액")
    @Builder.Default private BigDecimal netAmt = BigDecimal.ZERO;

    @Column(name = "CALC_TYPE", nullable = false, length = 20)
    @Comment("계산유형")
    @Builder.Default private String calcType = "BATCH ALL";

   @Enumerated(EnumType.STRING)
    @Column(name = "CALC_STATUS", nullable = false, length = 20)
    @Comment("계산 상태")
    @Builder.Default
    private CalcStatus calcStatus = CalcStatus.READY;


    @Column(name = "IS_PARTIAL", nullable = false, length = 1)
    @Comment("부분 정산")
    @Builder.Default private String isPartial = "N";

    @Column(name = "CALC_DATE", nullable = false)
    @Comment("계산일시")
    @Builder.Default private LocalDateTime calcDt = LocalDateTime.now();

    @Column(name = "CONFIRM_USER", length = 20)
    @Comment("확정자")
    private String confirmUser;

    @Column(name = "CONFIRM_DATE")
    @Comment("확정일시")
    private LocalDateTime confirmDate;

    @Column(name = "PDF_PATH", length = 255)
    private String pdfPath;

    @Column(name = "PDF_HASH_ID", length = 40)
    private String pdfHashId;

    @Column(name = "JOB_ID")
    private Long jobId;

    @Lob
    @Column(name = "REMARK")
    private String remark;

	
}
