package com.yeoun.sales.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yeoun.sales.dto.OrderListDTO;
import com.yeoun.sales.entity.Client;
import com.yeoun.sales.entity.OrderItem;
import com.yeoun.sales.entity.Orders;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.sales.repository.OrderItemRepository;
import com.yeoun.sales.repository.OrdersRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;

    @PersistenceContext
    private EntityManager em;

    /** 수주 목록 조회 */
    public List<OrderListDTO> search(
            String status,
            LocalDate startDate,
            LocalDate endDate,
            String keyword
    ) {
        return ordersRepository.searchOrders(status, startDate, endDate, keyword);
    }
    
   
    /** 거래처 자동완성 (CUSTOMER + ACTIVE 만) */

 public List<Map<String, String>> searchCustomer(String keyword) {
     
     // null/빈 문자열 처리
     if (keyword == null || keyword.trim().isEmpty()) {
         keyword = "";  // 또는 빈 리스트 반환
     }
     
     String searchKeyword = "%" + keyword.trim() + "%";
     
     List<Client> list = em.createQuery(
             "SELECT c FROM Client c " +
             "WHERE c.clientType = 'CUSTOMER' " +
             "AND c.statusCode = 'ACTIVE' " +
             "AND c.clientName LIKE :keyword " +
             "ORDER BY c.clientName",
             Client.class
     )
     .setParameter("keyword", searchKeyword)
     .getResultList();

     return list.stream()
             .map(c -> Map.of(
                     "clientId", c.getClientId(),
                     "clientName", c.getClientName()
             ))
             .toList();
 }


    /** 제품 목록 조회 (OrdersService에서 직접 JPQL 사용) */
    public List<ProductMst> getProducts() {
        return em.createQuery(
                "SELECT p FROM ProductMst p WHERE p.prdStatus = 'ACTIVE' ORDER BY p.prdName",
                ProductMst.class
        ).getResultList();
    }

    
    /*수주 등록*/
    @Transactional
    public void createOrder(String clientId,
                            String orderDate,
                            String deliveryDate,
                            String empId,
                            String orderMemo,
                            Map<String, String> params) {
    	
    	 // 🔥 주문번호 생성 (예: ORD + yyyyMMdd + 4자리)
        String orderId = generateOrderId();

        // 1) 수주 저장
        Orders order = Orders.builder()
        		.orderId(orderId) 
                .clientId(clientId)
                .orderDate(LocalDate.parse(orderDate))
                .deliveryDate(LocalDate.parse(deliveryDate))
                .empId(empId)
                .orderMemo(orderMemo)
                .build();

        ordersRepository.save(order);        

        // 2) item 저장
        int idx = 0;

        while (true) {
            String productId = params.get("items[" + idx + "][prdId]");

            if (productId == null) break;

            BigDecimal qty = new BigDecimal(params.get("items[" + idx + "][qty]"));
            BigDecimal unitPrice = new BigDecimal(params.get("items[" + idx + "][unitPrice]"));
            BigDecimal amount = new BigDecimal(params.get("items[" + idx + "][amount]"));
            String memoTxt = params.get("items[" + idx + "][memo]");

            OrderItem item = OrderItem.builder()
                    .orderId(orderId)
                    .productId(productId)
                    .orderQty(qty)
                    .unitPrice(unitPrice)
                    .totalPrice(amount)
                    .itemMemo(memoTxt)
                    .itemStatus("ORDER") // 기본값
                    .build();

            orderItemRepository.save(item);

            idx++;
        }
    }
    
    //주문번호 생성
    
    public String generateOrderId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 오늘 날짜 기준 마지막 주문번호 + 1
        String lastId = ordersRepository.findLastOrderId(today);

        int seq = 1;
        if (lastId != null) {
            seq = Integer.parseInt(lastId.substring(11)) + 1;
        }

        return "ORD" + today + "-" + String.format("%03d", seq);
    }

}
