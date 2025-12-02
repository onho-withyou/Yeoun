package com.yeoun.sales.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yeoun.sales.dto.OrderListDTO;
import com.yeoun.sales.entity.Client;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.sales.repository.OrdersRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;

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



}
