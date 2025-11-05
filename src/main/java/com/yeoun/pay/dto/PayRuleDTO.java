package com.yeoun.pay.dto;

import java.time.LocalDate;

import com.yeoun.pay.entity.PayRuleStatus;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 신규/수정 공용 요청 DTO */
@Data
public class PayRuleDTO {
    @NotNull
    private LocalDate startDate;     // 적용시작일
    private LocalDate endDate;       // 적용종료일(NULL=무기한)

    // 금액(선택)
    private Double baseAmt;
    private Double mealAmt;
    private Double transAmt;

    // 요율(0~1 범위 권장)
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double penRate;        // 0.045 = 4.5%
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double hlthRate;
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double empRate;
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double taxRate;

    @Min(1) @Max(31)
    private Integer payDay;        // 1~31
    private PayRuleStatus status;  // ACTIVE/INACTIVE
    private String remark;         // 비고
}
