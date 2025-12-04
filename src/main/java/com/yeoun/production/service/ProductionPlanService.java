package com.yeoun.production.service;

import com.yeoun.production.dto.PlanCreateItemDTO;
import com.yeoun.production.dto.ProductionPlanListDTO;
import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.entity.ProductionPlanItem;
import com.yeoun.production.enums.BomStatus;
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
        생산계획 생성 
    ================================ */
    @Transactional
    public String createPlan(List<PlanCreateItemDTO> items, String createdBy, String memo) {

        // 0) 검증
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("생산계획 생성 실패: 선택된 수주 항목이 없습니다.");
        }

        /* ============================================================
            1) 제품(PRD_ID) 동일한지 체크 → PLANS는 제품 1개 기준이므로
        ============================================================ */
        String prdId = null;

        for (PlanCreateItemDTO dto : items) {
            OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

            if (prdId == null) prdId = oi.getProductId();
            else if (!prdId.equals(oi.getProductId())) {
                throw new IllegalArgumentException("생산계획은 동일 제품만 묶어서 생성할 수 있습니다.");
            }
        }

        /* ============================================================
            2) 총 계획 수량 계산 (PLAN_QTY)
        ============================================================ */
        BigDecimal totalPlanQty = items.stream()
                .map(dto -> BigDecimal.valueOf(dto.getQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        /* ============================================================
            3) 생산계획 마스터 생성 (PRD_ID 기준)
        ============================================================ */
        String planId = generatePlanId();

        ProductionPlan plan = ProductionPlan.builder()
                .planId(planId)
                .prdId(prdId)
                .planQty(totalPlanQty.intValue()) // 마스터 총 생산수량
                .planDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7)) // 기본 납기일자 (원하면 수정)
                .status("PLANNING")
                .planMemo(memo)
                .createdBy(createdBy)
                .build();

        planRepo.save(plan);

        /* ============================================================
            4) 상세항목 생성 (PRODUCTION_PLAN_ITEM)
        ============================================================ */
        for (PlanCreateItemDTO dto : items) {

            OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

            ProductionPlanItem item = ProductionPlanItem.builder()
                    .planItemId(generatePlanItemId())
                    .planId(planId)
                    .prdId(prdId) // 동일 제품
                    .orderQty(oi.getOrderQty()) // 수주 수량
                    .planQty(BigDecimal.valueOf(dto.getQty())) // 계획 생산수량
                    .bomStatus(BomStatus.WAIT)
                    .partialQty(null)
                    .itemMemo("")
                    .createdBy(createdBy)
                    .build();

            itemRepo.save(item);
        }

        return planId;
    }

    
 // 엔티티 목록 조회
    public List<ProductionPlan> getPlanEntities() {
        return planRepo.findAllByOrderByCreatedAtDesc();
    }

    public ProductionPlan getPlanDetail(String planId) {
        return planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("생산계획 없음: " + planId));
    }

    public List<ProductionPlanItem> getPlanItems(String planId) {
        return itemRepo.findByPlanId(planId);
    }
    
 // 신규 DTO 목록 조회 (AG-Grid용)
    public List<ProductionPlanListDTO> getPlanList() {
        return planRepo.findPlanList();
    }

    

}
