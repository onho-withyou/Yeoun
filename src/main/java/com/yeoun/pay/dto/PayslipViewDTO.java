package com.yeoun.pay.dto;

import java.math.BigDecimal;
import com.yeoun.pay.enums.CalcStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class PayslipViewDTO {
    private Long payslipId;
    private String payYymm;
    private String empId;
    private String empName;   // 사원명
    private String deptId;
    private String deptName;  // 부서명
    private BigDecimal baseAmt;
    private BigDecimal alwAmt;
    private BigDecimal dedAmt;
    private BigDecimal netAmt;
    private BigDecimal totAmt;
    private CalcStatus calcStatus;
}
