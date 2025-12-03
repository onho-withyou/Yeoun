package com.yeoun.inventory.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InventoryAdjustRequestDTO {
    private Long ivId;          // path에서 세팅
    private String adjustType;  // INC / DEC
    private Long adjustQty;
    private String reason;
}
