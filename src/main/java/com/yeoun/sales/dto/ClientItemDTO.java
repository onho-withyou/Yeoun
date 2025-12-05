package com.yeoun.sales.dto;

import java.math.BigDecimal;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientItemDTO {

    private Long itemId;
    private String materialId;

    private String materialName;  
    private String unit;         

    private BigDecimal unitPrice;
    private BigDecimal moq;
    private String supplyAvailable;
}


