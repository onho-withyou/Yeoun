package com.yeoun.sales.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlanSuggestDTO {

    private String prdId;          // 제품 ID
    private String prdName;    // 제품명
    private int totalOrderQty;     // 총 주문 수량
    private int currentStock;      // 현재 재고
    private int shortageQty;       // 부족 수량 (생산해야 하는 양)
    private String needProduction; // YES / NO

    private List<OrderItemInfo> orderItems; // 묶인 수주 상세 리스트

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class OrderItemInfo {
        private Long orderItemId;
        private String orderId;
        private int orderQty;
        private String dueDate;
        private String clientName;
        private String managerName;
        private String managerTel;
        private String managerEmail;
        private String prdName;
    }
}
