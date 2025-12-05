package com.yeoun.sales.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ClientItemDTO {
    private String materialId;
    private BigDecimal unitPrice;
    private BigDecimal moq;
    private String unit;
}

