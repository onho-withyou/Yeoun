package com.yeoun.pay.dto;

import java.time.LocalDate;

import com.yeoun.pay.enums.ActiveStatus;

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
    private Double baseAmt; // 기본급
    private Double mealAmt;  // 식대
    private Double transAmt; // 교통비

    // 요율(0~1 범위 권장)
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double penRate;        // 국민연금 요율
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double hlthRate;       //건강보험 요율
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double empRate;			// 고용보험 요율
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double taxRate;			// 소득세율

    @Min(1) @Max(31)
    private Integer payDay;        // 지급일
    private ActiveStatus status;  // ACTIVE/INACTIVE
    private String remark;         // 비고
}
