package com.yeoun.sales.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.sales.dto.OrderListDTO;
import com.yeoun.sales.entity.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, String> {

	@Query("""
		    SELECT new com.yeoun.sales.dto.OrderListDTO(
		        o.orderId,
		        c.clientName,
		        o.orderDate,
		        o.deliveryDate,
		        o.orderStatus,
		        e.empName,      
		        o.orderMemo
		    )
		    FROM Orders o
		    LEFT JOIN Client c ON c.clientId = o.clientId
		    LEFT JOIN Emp e ON e.empId = o.empId   
		    WHERE (:status IS NULL OR o.orderStatus = :status)
		      AND (:startDate IS NULL OR o.orderDate >= :startDate)
		      AND (:endDate IS NULL OR o.deliveryDate <= :endDate)
		      AND (:keyword IS NULL OR 
		             c.clientName LIKE CONCAT('%', :keyword, '%') 
		          OR o.orderId LIKE CONCAT('%', :keyword, '%')
		      )
		    ORDER BY o.orderDate DESC
		""")
		List<OrderListDTO> searchOrders(
		    @Param("status") String status,
		    @Param("startDate") LocalDate startDate,
		    @Param("endDate") LocalDate endDate,
		    @Param("keyword") String keyword
		);


	
	//주문번호 생성
	@Query(value = """
		    SELECT ORDER_ID
		    FROM ORDERS
		    WHERE ORDER_ID LIKE 'ORD' || :today || '%'
		    ORDER BY ORDER_ID DESC
		    FETCH FIRST 1 ROWS ONLY
		""", nativeQuery = true)
		String findLastOrderId(@Param("today") String today);





}


