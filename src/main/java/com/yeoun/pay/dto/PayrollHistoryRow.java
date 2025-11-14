package com.yeoun.pay.dto;

import java.math.BigDecimal;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PayrollHistoryRow {

    private String empId;
    private String empName;
    private String deptName;
    private String payYymm;

    private BigDecimal baseAmt;
    private BigDecimal alwAmt;
    private BigDecimal dedAmt;
    private BigDecimal totAmt;
    private BigDecimal netAmt;

    private String calcStatus;
}
