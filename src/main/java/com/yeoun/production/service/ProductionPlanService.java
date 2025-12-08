package com.yeoun.production.service;

import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.production.dto.*;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {

    private final ProductionPlanRepository planRepo;
    private final ProductionPlanItemRepository itemRepo;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final EmpRepository employeeRepository;

    /* =============================================================
    공통 함수: OrderItem → OrderItemDTO 변환
 ============================================================= */
 private OrderItemDTO convertToOrderItemDTO(OrderItem oi) {

     // ⭐ EMP_ID → 직원명 조회 (없으면 "미지정")
     String empName = employeeRepository.findById(oi.getOrder().getEmpId())
             .map(emp -> emp.getEmpName())
             .orElse("미지정");

     return new OrderItemDTO(
             oi.getOrderItemId(),
             oi.getOrderId(),
             oi.getPrdId(),
             oi.getProduct().getPrdName(),
             oi.getOrderQty().intValue(),

             oi.getOrder().getClient().getClientName(),     // 거래처명
             oi.getOrder().getClient().getManagerName(),    // 담당자명
             oi.getOrder().getClient().getManagerTel(),     // 연락처
             oi.getOrder().getClient().getManagerEmail(),   // 이메일

             oi.getOrder().getOrderDate(),                  // 수주일자
             oi.getOrder().getDeliveryDate(),               // 납기일

             empName                                        // ⭐ 내부 담당자명
     );
 }


    /* ================================
        생산계획 ID 생성
    ================================ */
    private String generatePlanId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PLD" + today + "-";

        String last = planRepo.findLastPlanId(prefix);
        int seq = (last == null) ? 1 : Integer.parseInt(last.substring(last.lastIndexOf("-") + 1)) + 1;

        return prefix + String.format("%03d", seq);
    }

    /* ================================
        생산계획 상세 ID 생성
    ================================ */
    private String generatePlanItemId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PIM" + today + "-";

        String last = itemRepo.findLastPlanItemId(prefix);
        int seq = (last == null) ? 1 : Integer.parseInt(last.substring(last.lastIndexOf("-") + 1)) + 1;

        return prefix + String.format("%03d", seq);
    }

    /* ================================
        생산계획 생성 (수동)
    ================================ */
    @Transactional
    public String createPlan(List<PlanCreateItemDTO> items, String createdBy, String memo) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("생산계획 생성 실패: 선택된 수주 항목이 없습니다.");
        }

        String prdId = null;
        int totalPlanQty = 0;
        List<Long> orderItemIdList = new ArrayList<>();

        for (PlanCreateItemDTO dto : items) {
            OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new IllegalArgumentException("OrderItem 없음: " + dto.getOrderItemId()));

            orderItemIdList.add(oi.getOrderItemId());

            if (prdId == null) prdId = oi.getPrdId();
            else if (!prdId.equals(oi.getPrdId()))
                throw new IllegalArgumentException("생산계획은 동일 제품만 묶어서 생성할 수 있습니다.");

            totalPlanQty += dto.getQty();
        }

        String planId = generatePlanId();

        // 마스터 생성
        ProductionPlan plan = ProductionPlan.builder()
                .planId(planId)
                .prdId(prdId)
                .planQty(totalPlanQty)
                .planDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .status(ProductionStatus.PLANNING)
                .planMemo(memo)
                .createdBy(createdBy)
                .build();

        planRepo.save(plan);

        // 상세 생성
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

        // OrderItem 상태 변경
        orderItemIdList.forEach(orderItemRepository::updateStatusToPlanned);

        return planId;
    }

    /* ================================
       생산계획 목록 조회
    ================================ */
    public List<ProductionPlanListDTO> getPlanList() {
        return planRepo.findPlanList();
    }

    /* ================================
       생산 추천 목록 생성
    ================================ */
    
 public List<OrderPlanSuggestDTO> getPlanSuggestions(String group) {

     // 1) 제품별 확정 수주량, 납기, 건수 등을 그룹화 조회
     List<Map<String, Object>> groups = orderItemRepository.findConfirmedGrouped(group);

     // 2) 전체 재고 조회 (제품별)
     List<Map<String, Object>> stockList = inventoryRepository.findCurrentStockGrouped();

     Map<String, Integer> stockMap = new HashMap<>();
     for (Map<String, Object> s : stockList) {
         stockMap.put(
                 (String) s.get("prdId"),
                 ((BigDecimal) s.get("currentStock")).intValue()
         );
     }

     List<OrderPlanSuggestDTO> results = new ArrayList<>();

     // 3) 제품별 생산 추천 정보 계산
     for (Map<String, Object> g : groups) {

         String prdId = (String) g.get("prdId");
         String prdName = (String) g.get("prdName");

         int totalOrderQty = ((BigDecimal) g.get("totalOrderQty")).intValue();

         int orderCount = ((Number) g.get("orderCount")).intValue();   // ⭐ 수주건수
         LocalDate earliestDelivery = (LocalDate) g.get("earliestDeliveryDate"); // ⭐ 가장 빠른 납기

         int currentStock = stockMap.getOrDefault(prdId, 0);
         int shortageQty = Math.max(totalOrderQty - currentStock, 0);

         // 원자재/재고 부족 여부 표시
         String bomStatus = shortageQty > 0 ? "부족" : "정상";

         // 4) 해당 제품의 수주 상세 목록 가져오기
         List<Map<String, Object>> items = orderItemRepository.findItemsByProduct(prdId);

         List<OrderPlanSuggestDTO.OrderItemInfo> orderItems = items.stream()
                 .map(i -> new OrderPlanSuggestDTO.OrderItemInfo(
                         ((Number) i.get("ORDER_ITEM_ID")).longValue(),
                         (String) i.get("ORDER_ID"),
                         ((Number) i.get("ORDER_QTY")).intValue(),
                         (String) i.get("dueDate"),
                         (String) i.get("CLIENT_NAME"),
                         (String) i.get("MANAGER_NAME"),
                         (String) i.get("MANAGER_TEL"),
                         (String) i.get("MANAGER_EMAIL"),
                         (String) i.get("PRD_NAME")
                 ))
                 .toList();

         // 5) DTO 생성하여 리스트에 추가
         results.add(
                 OrderPlanSuggestDTO.builder()
                         .prdId(prdId)
                         .prdName(prdName)
                         .totalOrderQty(totalOrderQty)
                         .currentStock(currentStock)
                         .shortageQty(shortageQty)
                         .needProduction(shortageQty > 0 ? "YES" : "NO")
                         .orderCount(orderCount)  // ⭐ 추가
                         .earliestDeliveryDate(
                                 earliestDelivery != null
                                         ? earliestDelivery.toString()
                                         : "-"
                         )                       // ⭐ 추가
                         .bomStatus(bomStatus)  // ⭐ 원자재 상태
                         .orderItems(orderItems)
                         .build()
         );
     }

     return results;
 }


    /* ============================
        자동 추천 기반 생산계획 생성
    ============================ */
    @Transactional
    public String createAutoPlan(List<Map<String, Object>> requestList, String createdBy, String memo) {

        if (requestList == null || requestList.isEmpty()) {
            throw new IllegalArgumentException("자동 생산계획 생성 실패: 요청 데이터 없음");
        }

        StringBuilder resultMsg = new StringBuilder();

        for (Map<String, Object> req : requestList) {

            String prdId = (String) req.get("prdId");
            Integer planQty = (Integer) req.get("planQty");

            if (prdId == null || planQty == null)
                throw new IllegalArgumentException("잘못된 요청 데이터입니다.");

            String planId = generatePlanId();

            ProductionPlan plan = ProductionPlan.builder()
                    .planId(planId)
                    .prdId(prdId)
                    .planQty(planQty)
                    .planDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(7))
                    .status(ProductionStatus.PLANNING)
                    .planMemo(memo)
                    .createdBy(createdBy)
                    .build();

            planRepo.save(plan);

            // 상세 정보 저장
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
        생산계획 상세보기 모달
    ============================ */
    @Transactional(readOnly = true)
    public PlanDetailDTO getPlanDetailForModal(String planId) {

        ProductionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("생산계획 없음: " + planId));

        List<ProductionPlanItem> planItems = itemRepo.findByPlanId(planId);

        // 제품별 병합
        Map<String, ProductionPlanItemDTO> merged = new HashMap<>();

        for (ProductionPlanItem item : planItems) {

            String prdId = item.getPrdId();
            int qty = item.getPlanQty().intValue();

            merged.merge(prdId,
                    new ProductionPlanItemDTO(
                            item.getPlanItemId(),
                            item.getPrdId(),
                            item.getProduct().getPrdName(),
                            qty,
                            item.getBomStatus().name(),
                            item.getStatus().name()
                    ),
                    (oldVal, newVal) -> {
                        oldVal.setPlanQty(oldVal.getPlanQty() + qty);
                        return oldVal;
                    });
        }

        // 수주 매핑
        Map<String, List<OrderItemDTO>> orderItemMap = new HashMap<>();

        for (ProductionPlanItem item : planItems) {

            Long orderItemId = Long.valueOf(item.getOrderItemId());
            OrderItem oi = orderItemRepository.findById(orderItemId).orElse(null);

            if (oi != null) {
                OrderItemDTO dto = convertToOrderItemDTO(oi);
                orderItemMap.computeIfAbsent(item.getPrdId(), k -> new ArrayList<>()).add(dto);
            }
        }

        String itemName = planItems.isEmpty()
                ? ""
                : planItems.get(0).getProduct().getPrdName();

        return new PlanDetailDTO(
                plan.getPlanId(),
                plan.getCreatedAt().toString(),
                itemName,
                plan.getPlanQty(),
                plan.getStatus().name(),
                plan.getPlanMemo(),
                new ArrayList<>(merged.values()),
                orderItemMap
        );
    }


    /* ============================
        공통 조회 API
    ============================ */
    public List<OrderItemDTO> getOrderItemsByProduct(String prdId) {

        List<OrderItem> list = orderItemRepository.findByPrdId(prdId);

        List<OrderItemDTO> dtoList = new ArrayList<>();

        for (OrderItem oi : list) {
            dtoList.add(convertToOrderItemDTO(oi));  // ⭐ 공통 함수 사용
        }

        return dtoList;
    }

}
