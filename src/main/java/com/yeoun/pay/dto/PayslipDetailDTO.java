package com.yeoun.pay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayslipDetailDTO {
	private String payYymm;
    private String empId;
    private String empName;
    private String deptId;
    private String deptName;
    private String posName;

    private BigDecimal baseAmt;
    private BigDecimal alwAmt;
    private BigDecimal dedAmt;
    private BigDecimal netAmt;
    private BigDecimal incAmt;
    private BigDecimal totAmt;
    
    private String confirmUser;     
    private LocalDateTime confirmDate;
    
    private List<Item> items;
    

    @Data @Builder
    public static class Item {
        private String itemName;
        private BigDecimal amount;
        private String type; // ALLOWANCE / DEDUCTION
    }
    
    private List<PayslipItemDTO> payItems; // 지급 상세
    private List<PayslipItemDTO> dedItems; // 공제 상세
    
    
}