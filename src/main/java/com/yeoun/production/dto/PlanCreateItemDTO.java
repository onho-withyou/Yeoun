package com.yeoun.production.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanCreateItemDTO {
    private String orderId;
    private Long orderItemId;
    private int qty;
}
