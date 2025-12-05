package com.yeoun.production.service;

import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.production.dto.PlanCreateItemDTO;
import com.yeoun.production.dto.PlanDetailDTO;
import com.yeoun.production.dto.ProductionPlanItemDTO;
import com.yeoun.production.dto.ProductionPlanListDTO;
import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.entity.ProductionPlanItem;
import com.yeoun.production.enums.BomStatus;
import com.yeoun.production.enums.ProductionStatus;
import com.yeoun.production.repository.ProductionPlanItemRepository;
import com.yeoun.production.repository.ProductionPlanRepository;
import com.yeoun.sales.dto.OrderItemDTO;
import com.yeoun.sales.dto.OrderPlanSuggestDTO;
import com.yeoun.sales.entity.OrderItem;
import com.yeoun.sales.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


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

        int totalPlanQty = 0;  // ⭐ 제품 총량 합산

        for (PlanCreateItemDTO dto : items) {
            OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

            orderItemIdList.add(oi.getOrderItemId());

            if (prdId == null) prdId = oi.getPrdId();
            else if (!prdId.equals(oi.getPrdId())) {
                throw new IllegalArgumentException("생산계획은 동일 제품만 묶어서 생성할 수 있습니다.");
            }

            totalPlanQty += dto.getQty();   // ⭐ 수량 누적
        }

        String planId = generatePlanId();

        /* -------------------------------------
           1) 생산계획 마스터 1개 생성
        -------------------------------------- */
        ProductionPlan plan = ProductionPlan.builder()
                .planId(planId)
                .prdId(prdId)
                .planQty(totalPlanQty)   // ⭐ 총합 수량
                .planDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status(ProductionStatus.PLANNING)
                .planMemo(memo)
                .createdBy(createdBy)
                .build();

        planRepo.save(plan);

        /* -------------------------------------
           2) 상세 항목은 제품당 1개만 생성
        -------------------------------------- */      
     for (PlanCreateItemDTO dto : items) {

         OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                 .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

         ProductionPlanItem detail = ProductionPlanItem.builder()
                 .planItemId(generatePlanItemId())
                 .planId(planId)
                 .prdId(prdId)
                 .orderItemId(oi.getOrderItemId())   
                 .orderQty(oi.getOrderQty())         
                 .planQty(oi.getOrderQty())         
                 .bomStatus(BomStatus.WAIT)
                 .status(ProductionStatus.PLANNING)
                 .itemMemo("")
                 .createdBy(createdBy)
                 .build();

         itemRepo.save(detail);
     }


        /* -------------------------------------
           3) OrderItem 상태를 일괄 PLANNED로 변경
        -------------------------------------- */
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
	            .status(ProductionStatus.PLANNING) 
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
                		    .orderItemId(oi.getOrderItemId())   
                		    .orderQty(oi.getOrderQty())
                		    .planQty(oi.getOrderQty())
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
 
 

 /* ============================
	 생산계획 상세 모달 서비스
	============================ */
@Transactional(readOnly = true)
public PlanDetailDTO getPlanDetailForModal(String planId) {

 // 1) 생산계획 단건 조회
 ProductionPlan plan = planRepo.findById(planId)
         .orElseThrow(() -> new IllegalArgumentException("생산계획을 찾을 수 없습니다."));

 // 2) PLAN_ITEM 목록 조회
 List<ProductionPlanItem> planItems = itemRepo.findByPlanId(planId);

 /* =======================================================
 ★ 2-1) PLAN_ITEM → DTO (제품별로 1개로 병합)
======================================================= */
Map<String, ProductionPlanItemDTO> merged = new HashMap<>();

for (ProductionPlanItem item : planItems) {

  String prdId = item.getPrdId();
  int qty = item.getPlanQty().intValue();

  if (merged.containsKey(prdId)) {
      // 이미 등록된 제품 → 수량 합산
      ProductionPlanItemDTO dto = merged.get(prdId);
      dto.setPlanQty(dto.getPlanQty() + qty);
  } else {
      // 신규 등록
      merged.put(prdId,
              new ProductionPlanItemDTO(
                      item.getPlanItemId(),
                      item.getPrdId(),
                      item.getProduct().getPrdName(),
                      qty,
                      item.getBomStatus().name(),
                      item.getStatus().name()
              )
      );
  }
}

List<ProductionPlanItemDTO> planItemDTOs = new ArrayList<>(merged.values());



 // 3) 제품별 수주(OrderItem) 매핑
 Map<String, List<OrderItemDTO>> orderItemMap = new HashMap<>();

 for (ProductionPlanItem item : planItems) {

     Long orderItemId = Long.valueOf(item.getOrderItemId());

     OrderItem oi = orderItemRepository.findById(orderItemId)
             .orElse(null);

     if (oi != null) {
    	 OrderItemDTO dto = new OrderItemDTO(
    			    oi.getOrderItemId(),
    			    oi.getOrderId(),
    			    oi.getPrdId(),
    			    oi.getProduct().getPrdName(),
    			    oi.getOrderQty().intValue(),
    			    oi.getOrder().getClient().getClientName(),  // ⭐ 거래처명
    			    oi.getOrder().getOrderDate(),        // ⭐ 수주일자
    			    oi.getOrder().getDeliveryDate()     // ⭐ 납기일
    			    
    			);
    
         orderItemMap.computeIfAbsent(item.getPrdId(), k -> new ArrayList<>()).add(dto);
     }
 }

 // 4) 제품명 가져오기
 String itemName = planItems.isEmpty()
         ? ""
         : planItems.get(0).getProduct().getPrdName();

 // 5) 최종 DTO 생성 후 반환
 return new PlanDetailDTO(
         plan.getPlanId(),
         plan.getCreatedAt().toString(),
         itemName,
         plan.getPlanQty(),
         plan.getStatus().name(),
         planItemDTOs,
         orderItemMap
 );
}
  
}
