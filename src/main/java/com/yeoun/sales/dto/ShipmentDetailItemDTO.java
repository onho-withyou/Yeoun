package com.yeoun.sales.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ShipmentDetailItemDTO {

    private String prdName;
    private BigDecimal orderQty;
    private BigDecimal stockQty;
    private boolean reservable;

    public ShipmentDetailItemDTO(
            String prdName,
            BigDecimal orderQty,
            BigDecimal stockQty,
            Object reservable
    ) {
        this.prdName = prdName;
        this.orderQty = orderQty;
        this.stockQty = stockQty;

        if (reservable instanceof Number n) {
            this.reservable = n.intValue() == 1;
        }
    }
}
