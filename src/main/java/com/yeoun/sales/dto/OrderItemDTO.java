package com.yeoun.sales.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long orderItemId;    // PK
    private String orderId;      // 수주번호
    private String prdId;        // 제품ID
    private String prdName;      // 제품명
    private Integer orderQty;    // 주문수량
    
    private String clientName;       // ⭐ 거래처명 추가    
    private LocalDate orderDate;     // ⭐ 수주일자 추가
    private LocalDate deliveryDate;  // ⭐ 납기일 (기존 dueDate → deliveryDate로 이름 변경)
   
}

