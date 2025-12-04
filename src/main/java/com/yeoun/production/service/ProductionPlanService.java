package com.yeoun.production.service;

import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.production.dto.PlanCreateItemDTO;
import com.yeoun.production.dto.ProductionPlanListDTO;
import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.entity.ProductionPlanItem;
import com.yeoun.production.enums.BomStatus;
import com.yeoun.production.enums.ProductionStatus;
import com.yeoun.production.repository.ProductionPlanItemRepository;
import com.yeoun.production.repository.ProductionPlanRepository;
import com.yeoun.sales.dto.OrderPlanSuggestDTO;
import com.yeoun.sales.entity.OrderItem;
import com.yeoun.sales.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {

    private final ProductionPlanRepository planRepo;
    private final ProductionPlanItemRepository itemRepo;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;

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
=============================== */
@Transactional
public String createPlan(List<PlanCreateItemDTO> items, String createdBy, String memo) {

    if (items == null || items.isEmpty()) {
        throw new IllegalArgumentException("생산계획 생성 실패: 선택된 수주 항목이 없습니다.");
    }

    String prdId = null;
    List<Long> orderItemIdList = new ArrayList<>();

    for (PlanCreateItemDTO dto : items) {
        OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

        orderItemIdList.add(oi.getOrderItemId()); // 상태변경 대상 수집

        if (prdId == null) prdId = oi.getPrdId();
        else if (!prdId.equals(oi.getPrdId())) {
            throw new IllegalArgumentException("생산계획은 동일 제품만 묶어서 생성할 수 있습니다.");
        }
    }

    // 총 생산계획수량
    BigDecimal totalPlanQty = items.stream()
            .map(dto -> BigDecimal.valueOf(dto.getQty()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    String planId = generatePlanId();

    ProductionPlan plan = ProductionPlan.builder()
            .planId(planId)
            .prdId(prdId)
            .planQty(totalPlanQty.intValue())
            .planDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(7))
            .status(ProductionStatus.PLANNING) // ★ ENUM 적용
            .planMemo(memo)
            .createdBy(createdBy)
            .build();

    planRepo.save(plan);

    // 상세 생성
    for (PlanCreateItemDTO dto : items) {

        OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

        ProductionPlanItem item = ProductionPlanItem.builder()
                .planItemId(generatePlanItemId())
                .planId(planId)
                .prdId(prdId)
                .orderQty(oi.getOrderQty())
                .planQty(BigDecimal.valueOf(dto.getQty()))
                .bomStatus(BomStatus.WAIT)
                .status(ProductionStatus.PLANNING)
                .itemMemo("")
                .createdBy(createdBy)
                .build();

        itemRepo.save(item);
    }

    /* =====================================
        OrderItem 상태 변경 PLANNED
    ===================================== */
    orderItemIdList.forEach(orderItemRepository::updateStatusToPlanned);

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

    
    //자동 그룹화 + 부족수량 계산 + 생산계획 추천 기능
    public List<OrderPlanSuggestDTO> getPlanSuggestions(String group) {

        // 1) 확정된 수주 그룹 조회
        List<Map<String, Object>> groups = orderItemRepository.findConfirmedGrouped(group);

        // 2) 전체 재고를 한 번만 조회
        List<Map<String, Object>> stockList = inventoryRepository.findCurrentStockGrouped();

        // 3) 재고를 Map<String, Integer> 형태로 변환
        Map<String, Integer> stockMap = new HashMap<>();
        for (Map<String, Object> s : stockList) {
            String itemId = (String) s.get("prdId");
            int currentStock = ((BigDecimal) s.get("currentStock")).intValue();
            stockMap.put(itemId, currentStock);
        }

        List<OrderPlanSuggestDTO> results = new ArrayList<>();

        // 4) 제품별 수주량 + 재고 비교
        for (Map<String, Object> g : groups) {

            String prdId = (String) g.get("prdId");
            String prdName = (String) g.get("prdName");
            int totalOrderQty = ((BigDecimal) g.get("totalOrderQty")).intValue();

            // ★ 재고가 있으면 가져오고, 없으면 0으로 처리
            int currentStock = stockMap.getOrDefault(prdId, 0);

            int shortageQty = totalOrderQty - currentStock;
            if (shortageQty < 0) shortageQty = 0;

            // 상세 수주 목록 조회
            List<Map<String, Object>> items =
                    orderItemRepository.findItemsByProduct(prdId);

            List<OrderPlanSuggestDTO.OrderItemInfo> orderItems = items.stream()
                    .map(i -> new OrderPlanSuggestDTO.OrderItemInfo(
                            ((Number) i.get("ORDER_ITEM_ID")).longValue(),
                            (String) i.get("ORDER_ID"),
                            ((Number) i.get("ORDER_QTY")).intValue(),
                            (String) i.get("dueDate")
                    ))
                    .toList();

            results.add(
                    OrderPlanSuggestDTO.builder()
                            .prdId(prdId)
                            .prdName(prdName)
                            .totalOrderQty(totalOrderQty)
                            .currentStock(currentStock)
                            .shortageQty(shortageQty)
                            .needProduction(shortageQty > 0 ? "YES" : "NO")
                            .orderItems(orderItems)
                            .build()
            );
        }

        return results;
    }


    /* ============================================================
    자동 추천 기반 생산계획 생성
 ============================================================ */
 @Transactional
 public String createAutoPlan(List<Map<String, Object>> requestList, String createdBy) {

     if (requestList == null || requestList.isEmpty()) {
         throw new IllegalArgumentException("자동 생산계획 생성 실패: 요청 데이터 없음");
     }

     StringBuilder resultMsg = new StringBuilder();

     for (Map<String, Object> req : requestList) {

         String prdId = (String) req.get("prdId");
         Integer planQty = (Integer) req.get("planQty");

         if (prdId == null || planQty == null) {
             throw new IllegalArgumentException("잘못된 요청 데이터입니다.");
         }

         /* ---------------------------------------------
         1) 마스터 생산계획 생성
	    --------------------------------------------- */
	    String planId = generatePlanId();
	
	    ProductionPlan plan = ProductionPlan.builder()
	            .planId(planId)
	            .prdId(prdId)
	            .planQty(planQty)
	            .planDate(LocalDate.now())
	            .dueDate(LocalDate.now().plusDays(7))
	            .status(ProductionStatus.PLANNING) // ★ ENUM 적용
	            .planMemo("추천 기반 자동 생성 계획")
	            .createdBy(createdBy)
	            .build();
	
	    planRepo.save(plan);



         /* ---------------------------------------------
             2) 상세 항목 생성
         --------------------------------------------- */
         List<Map<String, Object>> orderItems =
                 (List<Map<String, Object>>) req.get("orderItems");

         if (orderItems != null) {

             for (Map<String, Object> item : orderItems) {

                 Long orderItemId = Long.valueOf(item.get("orderItemId").toString());

                 OrderItem oi = orderItemRepository.findById(orderItemId)
                         .orElseThrow(() -> new IllegalArgumentException("OrderItem 찾을 수 없음: " + orderItemId));

                 ProductionPlanItem detail = ProductionPlanItem.builder()
                         .planItemId(generatePlanItemId())
                         .planId(planId)
                         .prdId(prdId)
                         .orderQty(oi.getOrderQty())
                         .planQty(oi.getOrderQty())   // 상세는 주문수량 기준
                         .bomStatus(BomStatus.WAIT)
                         .status(ProductionStatus.PLANNING)
                         .createdBy(createdBy)
                         .build();

                 itemRepo.save(detail);
             }
         }

         resultMsg.append(planId).append(" 생성완료, ");
     }

     return resultMsg.toString();
 }

}
