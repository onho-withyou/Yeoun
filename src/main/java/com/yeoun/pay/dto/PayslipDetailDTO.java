package com.yeoun.pay.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayslipDetailDTO {
    private String empId;
    private String empName;
    private String deptName;

    private BigDecimal baseAmt;
    private BigDecimal alwAmt;
    private BigDecimal dedAmt;
    private BigDecimal netAmt;

    private List<Item> items;

    @Data @Builder
    public static class Item {
        private String itemName;
        private BigDecimal amount;
        private String type; // ALLOWANCE / DEDUCTION
    }
}