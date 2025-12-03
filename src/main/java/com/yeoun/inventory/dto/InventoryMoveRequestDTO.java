package com.yeoun.inventory.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InventoryMoveRequestDTO {
    private Long ivId;          // path에서 세팅
    private Long moveLocationId;
    private Long moveAmount;
}
