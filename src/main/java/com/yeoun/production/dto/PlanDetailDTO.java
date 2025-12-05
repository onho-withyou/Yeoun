package com.yeoun.production.dto;

import java.util.List;
import java.util.Map;

import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.entity.ProductionPlanItem;
import com.yeoun.sales.dto.OrderItemDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlanDetailDTO {

    private String planId;
    private String createdAt;
    private String itemName;   // 🔥 제품명
    private Integer planQty;   // 🔥 이미 PLAN_QTY 있으니 이거 사용
    private String status;

    private List<ProductionPlanItemDTO> planItems;
    private Map<String, List<OrderItemDTO>> orderItemMap;
}
