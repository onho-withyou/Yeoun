package com.yeoun.sales.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yeoun.sales.dto.OrderDetailDTO;
import com.yeoun.sales.dto.OrderItemDTO;
import com.yeoun.sales.dto.OrderListDTO;
import com.yeoun.sales.entity.Client;
import com.yeoun.sales.entity.OrderItem;
import com.yeoun.sales.entity.Orders;
import com.yeoun.sales.enums.OrderItemStatus;
import com.yeoun.sales.enums.OrderStatus;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.sales.repository.OrderItemRepository;
import com.yeoun.sales.repository.OrdersRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmpRepository empRepository;

    @PersistenceContext
    private EntityManager em;

    /* ============================================================
       1) 수주 목록 조회
    ============================================================ */
    public List<OrderListDTO> search(
    	    String status,
    	    LocalDate startDate,
    	    LocalDate endDate,
    	    String keyword
    	) {
    	    OrderStatus statusEnum = null;

    	    if (status != null && !status.isBlank()) {
    	        statusEnum = OrderStatus.valueOf(status);
    	    }

    	    return ordersRepository.searchOrders(
    	        statusEnum,
    	        startDate,
    	        endDate,
    	        keyword
    	    );
    	}

    /* ============================================================
       2) 거래처 자동완성
    ============================================================ */
    public List<Map<String, String>> searchCustomer(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = "";
        }

        String search = "%" + keyword.trim() + "%";

        List<Client> list = em.createQuery(
                        "SELECT c FROM Client c " +
                        "WHERE c.clientType = 'CUSTOMER' " +
                        "AND c.statusCode = 'ACTIVE' " +
                        "AND c.clientName LIKE :kw " +
                        "ORDER BY c.clientName", Client.class)
                .setParameter("kw", search)
                .getResultList();

        return list.stream()
                .map(c -> Map.of(
                        "clientId", c.getClientId(),
                        "clientName", c.getClientName()
                ))
                .toList();
    }


    /* ============================================================
       3) 제품 목록 조회
    ============================================================ */
    public List<ProductMst> getProducts() {
        return em.createQuery(
                "SELECT p FROM ProductMst p " +
                "WHERE p.prdStatus = 'ACTIVE' " +
                "ORDER BY p.prdName", ProductMst.class
        ).getResultList();
    }


    /* ============================================================
       4) 신규 수주 등록 (🔥 최신 완성본)
    ============================================================ */
    @Transactional
    public void createOrder(
            String clientId,
            String orderDate,
            String deliveryDate,
            String empId,
            String managerName,
            String managerTel,
            String managerEmail,
            String postcode,
            String addr,
            String addrDetail,
            String orderMemo,
            HttpServletRequest req
    ) {
    	
    	  /* -----------------------------------
         * 0) 거래처(Client) 조회 
         ------------------------------------ */
        Client clientEntity = em.find(Client.class, clientId);
        if (clientEntity == null) {
            throw new IllegalArgumentException("거래처 정보를 찾을 수 없습니다: " + clientId);
        }

        /* -----------------------------
           1) 주문번호 생성
        ----------------------------- */
        String orderId = generateOrderId();


        /* -----------------------------
           2) 수주 마스터 저장
        ----------------------------- */
        Orders order = Orders.builder()
                .orderId(orderId)
                .client(clientEntity) 
                .empId(empId)
                .orderDate(LocalDate.parse(orderDate))
                .deliveryDate(LocalDate.parse(deliveryDate))
                .managerName(nvl(managerName))
                .managerTel(nvl(managerTel))
                .managerEmail(nvl(managerEmail))
                .postcode(nvl(postcode))
                .addr(nvl(addr))
                .addrDetail(nvl(addrDetail))
                .orderMemo(nvl(orderMemo))
                .orderStatus(OrderStatus.REQUEST)
                .build();

        ordersRepository.save(order);



        /* -----------------------------
           3) 아이템 저장
        ----------------------------- */
        int idx = 0;

        while (true) {

            String prdId = req.getParameter("items[" + idx + "][prdId]");
            if (prdId == null) break;

            String qtyStr    = req.getParameter("items[" + idx + "][qty]");
            String priceStr  = req.getParameter("items[" + idx + "][unitPrice]");
            String amountStr = req.getParameter("items[" + idx + "][amount]");
            String memo      = req.getParameter("items[" + idx + "][memo]");

            OrderItem item = OrderItem.builder()
                    .orderId(orderId)
                    .prdId(prdId)
                    .orderQty(new BigDecimal(qtyStr))
                    .unitPrice(new BigDecimal(priceStr))
                    .totalPrice(new BigDecimal(amountStr))
                    .itemMemo(memo)
                    .itemStatus(OrderItemStatus.REQUEST.name())
                    .build();

            orderItemRepository.save(item);

            idx++;
        }
    }


    /* ============================================================
       4) 주문번호 생성 로직
       ORD + yyyyMMdd + - + 3자리 시퀀스
    ============================================================ */
    public String generateOrderId() {

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String lastId = ordersRepository.findLastOrderId(today);

        int seq = 1;

        if (lastId != null) {
            String seqStr = lastId.substring(lastId.lastIndexOf("-") + 1);
            seq = Integer.parseInt(seqStr) + 1;
        }

        return "ORD" + today + "-" + String.format("%03d", seq);
    }



    /* ============================================================
      5) Null 방지 헬퍼
    ============================================================ */
    private String nvl(String v) {
        return (v == null ? "" : v);
    }
    
    
    /* ============================================================
    6) 생산계획 수주항목조회
  ============================================================ */
    
    public List<Map<String, Object>> getOrderItemsForPlan(String group) {
        return ordersRepository.findOrderItemsForPlan(group);
    }

    public List<OrderItemDTO> getConfirmedOrderItems() {
        return orderItemRepository.findConfirmedOrderItems();
    }


    /* ============================================================
    7) 수주 상세 조회
 ============================================================ */
  
 public OrderDetailDTO getOrderDetail(String orderId) {

     // 1) 수주 마스터 조회
     Orders order = ordersRepository.findById(orderId)
             .orElseThrow(() ->
                     new IllegalArgumentException("수주 정보가 없습니다. orderId=" + orderId)
             );

     // 2) 수주 아이템 조회
     List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

     // 3) 아이템 DTO 변환
     List<OrderItemDTO> itemDTOs = items.stream()
             .map(item -> OrderItemDTO.builder()
                     .orderItemId(item.getOrderItemId())
                     .prdId(item.getPrdId())
                     .prdName(item.getProduct().getPrdName())
                     .orderQty(item.getOrderQty())
                     .unitPrice(item.getUnitPrice())
                     .totalPrice(item.getTotalPrice())
                     .itemMemo(item.getItemMemo())
                     .itemStatus(item.getItemStatus())
                     .build()
             )
             .toList();
     
  //  3-0) 총 합계 계산 
     BigDecimal totalAmount = itemDTOs.stream()
             .map(OrderItemDTO::getTotalPrice)
             .reduce(BigDecimal.ZERO, BigDecimal::add);

     // ✅ 3-1) 내부 담당자(empId → empName) 조회 
     String empName = empRepository.findById(order.getEmpId())
             .map(emp -> emp.getEmpName())
             .orElse("미지정");

     // 4) 상세 DTO 생성
     return OrderDetailDTO.builder()
             .orderId(order.getOrderId())
             .clientId(order.getClient().getClientId())
             .clientName(order.getClient().getClientName())
             .orderDate(order.getOrderDate())
             .deliveryDate(order.getDeliveryDate())
             .orderStatus(order.getOrderStatus().name())
             .managerName(order.getManagerName())
             .managerTel(order.getManagerTel())
             .managerEmail(order.getManagerEmail())
             .postcode(order.getPostcode())
             .addr(order.getAddr())
             .addrDetail(order.getAddrDetail())
             .empId(order.getEmpId())
             .empName(empName)          // ⭐ 이제 정상
             .orderMemo(order.getOrderMemo())
             .items(itemDTOs)
             .totalAmount(totalAmount)
             .build();
 }

 // =========================
 // 수주 상태 변경
 // =========================
	 @Transactional
	 public void changeStatus(String orderId, OrderStatus status) {
	
	     Orders order = ordersRepository.findByOrderId(orderId)
	             .orElseThrow(() -> new IllegalArgumentException("수주 내역이 없습니다."));
	
	     order.changeStatus(status);
	 }


}
