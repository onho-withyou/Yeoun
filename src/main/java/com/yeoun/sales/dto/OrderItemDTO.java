package com.yeoun.sales.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String orderItemId;
    private String orderId;
    private String prdId;
    private String productName;
    private Integer orderQty;
    private LocalDate dueDate;
}

