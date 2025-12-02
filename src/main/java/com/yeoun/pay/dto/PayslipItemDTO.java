package com.yeoun.pay.dto;

import java.math.BigDecimal;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PayslipItemDTO {
    private String itemName;
    private BigDecimal amount;
    private Integer sortNo;
}
