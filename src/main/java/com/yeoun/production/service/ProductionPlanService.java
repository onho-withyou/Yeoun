package com.yeoun.production.service;

import com.yeoun.production.dto.PlanCreateItemDTO;
import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.entity.ProductionPlanItem;
import com.yeoun.production.repository.ProductionPlanItemRepository;
import com.yeoun.production.repository.ProductionPlanRepository;
import com.yeoun.sales.entity.OrderItem;
import com.yeoun.sales.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {

    private final ProductionPlanRepository planRepo;
    private final ProductionPlanItemRepository itemRepo;
    private final OrderItemRepository orderItemRepository;

    /* ================================
        생산계획 ID 생성
    ================================ */
    private String generatePlanId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PLD" + today + "-";

        String last = planRepo.findLastPlanId(prefix);
        int seq = 1;

        if (last != null) {
            seq = Integer.parseInt(last.substring(last.lastIndexOf("-") + 1)) + 1;
        }

        return prefix + String.format("%03d", seq);
    }

    /* ================================
        생산계획 상세 ID 생성
    ================================ */
    private String generatePlanItemId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PIM" + today + "-";

        String last = itemRepo.findLastPlanItemId(prefix);
        int seq = 1;

        if (last != null) {
            seq = Integer.parseInt(last.substring(last.lastIndexOf("-") + 1)) + 1;
        }

        return prefix + String.format("%03d", seq);
    }

    /* ================================
        생산계획 생성 (수정된 로직)
        → 여러 orderItem 기반
    ================================ */
    @Transactional
    public String createPlan(List<PlanCreateItemDTO> items, String createdBy, String memo) {

        // 0) items 비었는지 체크
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("생산계획 생성 실패: 선택된 수주 항목이 없습니다.");
        }

        // 1) 생산계획 마스터 생성
        String planId = generatePlanId();

        String orderIdForDisplay = items.get(0).getOrderId();  // 대표 수주번호

        ProductionPlan plan = ProductionPlan.builder()
                .planId(planId)
                .orderId(orderIdForDisplay)
                .planDate(LocalDate.now())
                .status("PLANNING")
                .planMemo(memo)
                .createdBy(createdBy)
                .build();

        planRepo.save(plan);

        // 2) 생산계획 상세 생성
        for (PlanCreateItemDTO dto : items) {

            // OrderItem 가져오기
            OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

            ProductionPlanItem item = ProductionPlanItem.builder()
                    .planItemId(generatePlanItemId())
                    .planId(planId)
                    .prdId(oi.getProductId())
                    .orderQty(oi.getOrderQty())   // BigDecimal 그대로 사용
                    .planQty(BigDecimal.valueOf(dto.getQty()))
                    .bomReadyYn("N")
                    .itemMemo("")
                    .createdBy(createdBy)
                    .build();


            itemRepo.save(item);
        }

        return planId;
    }
    
    public List<ProductionPlan> getPlanList() {
        return planRepo.findAllByOrderByCreatedAtDesc();
    }

    public ProductionPlan getPlanDetail(String planId) {
        return planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("생산계획 없음: " + planId));
    }

    public List<ProductionPlanItem> getPlanItems(String planId) {
        return itemRepo.findByPlanId(planId);
    }

}
