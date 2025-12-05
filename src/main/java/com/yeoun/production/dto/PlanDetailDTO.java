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

    private ProductionPlan plan;
    private List<ProductionPlanItem> planItems;
    private Map<String, List<OrderItemDTO>> orderItemMap;
}