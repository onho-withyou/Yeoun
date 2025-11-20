package com.yeoun.pay.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PayCalcStatusDTO {

    private long totalCount;       // 전체 건수
    private long simulatedCount;   // 가계산 건수
    private long calculatedCount;  // 계산완료 건수
    private long confirmedCount;   // 확정완료 건수
    private String calcStatus;     // 현재 월의 대표 상태
    private String payYymm;    
    private BigDecimal totAmt;
    private BigDecimal dedAmt;
    private BigDecimal netAmt;


    // ===== Thymeleaf 표시용 =====
    public boolean isSimulated() {
        return simulatedCount > 0;
    }

    public boolean isCalculated() {
        return calculatedCount > 0;
    }

    public boolean isConfirmed() {
        return confirmedCount > 0;
    }
}