package com.yeoun.sales.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ShipmentListDTO {

    private String orderId;
    private String clientName;
    private String prdName;
    private int orderQty;
    private int stockQty;
    private String dueDate;
    private String status;
}
