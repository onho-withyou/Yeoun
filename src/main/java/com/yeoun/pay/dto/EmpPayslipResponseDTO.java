package com.yeoun.pay.dto;

import java.util.List;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmpPayslipResponseDTO {

    // projection 결과 (기본 정보)
    private EmpPayslipDetailDTO header;

    // 항목 상세
    private List<EmpPayslipItem> items;

    @Data @Builder
    public static class EmpPayslipItem {
        private String itemName;
        private BigDecimal amount;
        private String type;
    }
}
