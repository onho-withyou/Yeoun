package com.yeoun.sales.repository;

import com.yeoun.sales.dto.OrderItemDTO;
import com.yeoun.sales.entity.OrderItem;

import java.util.List;
import java.util.Map;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByOrderId(String orderId);	
	
	@Query(value = """
		    SELECT 
		        oi.ORDER_ITEM_ID AS orderItemId,
		        oi.ORDER_ID AS orderId,
		        oi.PRD_ID AS prdId,
		        pm.PRD_NAME AS prdName,
		        oi.ORDER_QTY AS orderQty,
		        o.DUE_DATE AS dueDate
		    FROM ORDER_ITEM oi
		    JOIN ORDERS o ON o.ORDER_ID = oi.ORDER_ID
		    JOIN PRODUCT_MST pm ON pm.PRD_ID = oi.PRD_ID
		    WHERE o.ORDER_STATUS = 'CONFIRMED'
		    ORDER BY o.DUE_DATE
		    """, nativeQuery = true)
		List<OrderItemDTO> findConfirmedOrderItems();	
	
	// 1) 확정된 수주를 제품별로 그룹화
	@Query(value = """
		    SELECT
		        oi.PRD_ID AS prdId,
		        p.PRD_NAME AS prdName,
		        SUM(oi.ORDER_QTY) AS totalOrderQty
		    FROM ORDER_ITEM oi
		    JOIN PRODUCT_MST p ON p.PRD_ID = oi.PRD_ID
		    JOIN ORDERS o ON o.ORDER_ID = oi.ORDER_ID
		    WHERE o.ORDER_STATUS = 'CONFIRMED'
		      AND (:group = '' OR :group IS NULL OR p.ITEM_NAME = :group)
		    GROUP BY oi.PRD_ID, p.PRD_NAME
		""", nativeQuery = true)
		List<Map<String,Object>> findConfirmedGrouped(@Param("group") String group);



    // 2) 특정 제품에 대한 확정된 수주 상세 조회
    @Query(value = """
        SELECT 
            oi.ORDER_ITEM_ID,
            oi.ORDER_ID,
            oi.PRD_ID,
            oi.ORDER_QTY,
            TO_CHAR(o.DELIVERY_DATE, 'YYYY-MM-DD') AS dueDate
        FROM ORDER_ITEM oi
        JOIN ORDERS o ON o.ORDER_ID = oi.ORDER_ID
        WHERE o.ORDER_STATUS = 'CONFIRMED'
          AND oi.PRD_ID = :prdId
    """, nativeQuery = true)
    List<Map<String,Object>> findItemsByProduct(@Param("prdId") String prdId);

}
