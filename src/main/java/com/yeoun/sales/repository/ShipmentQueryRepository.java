package com.yeoun.sales.repository;

import com.yeoun.sales.dto.ShipmentListDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ShipmentQueryRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * 출하 목록 검색
     */
    public List<ShipmentListDTO> search(
            String startDate,
            String endDate,
            String keyword,
            String status
    ) {

        String sql = """
            SELECT
                oi.ORDER_ID              AS orderId,
                c.CLIENT_NAME            AS clientName,
                p.PRD_NAME               AS prdName,
                oi.ORDER_QTY             AS orderQty,

                /* 현재 재고 */
                NVL((
                    SELECT SUM(iv.IV_AMOUNT - iv.EXPECT_OB_AMOUNT)
                    FROM INVENTORY iv
                    WHERE iv.ITEM_ID = oi.PRD_ID
                ), 0) AS stockQty,

                o.DELIVERY_DATE          AS dueDate,

                /* 출하상태 */
                CASE 
                    WHEN EXISTS (SELECT 1 FROM SHIPMENT s WHERE s.ORDER_ID = oi.ORDER_ID)
                        THEN 'RESERVED'
                    WHEN NVL((
                        SELECT SUM(iv.IV_AMOUNT - iv.EXPECT_OB_AMOUNT)
                        FROM INVENTORY iv WHERE iv.ITEM_ID = oi.PRD_ID
                    ), 0) < oi.ORDER_QTY
                        THEN 'LACK'
                    ELSE 'READY'
                END AS shipmentStatus
            FROM ORDER_ITEM oi
            JOIN ORDERS o       ON oi.ORDER_ID = o.ORDER_ID
            JOIN PRODUCT_MST p  ON oi.PRD_ID = p.PRD_ID
            JOIN CLIENT c       ON o.CLIENT_ID = c.CLIENT_ID
            WHERE 1=1
        """;

        // 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql += " AND o.DELIVERY_DATE >= TO_DATE('" + startDate + "', 'YYYY-MM-DD') ";
        }

        if (endDate != null && !endDate.isEmpty()) {
            sql += " AND o.DELIVERY_DATE <= TO_DATE('" + endDate + "', 'YYYY-MM-DD') ";
        }

        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (oi.ORDER_ID LIKE '%" + keyword + "%' OR c.CLIENT_NAME LIKE '%" + keyword + "%' OR p.PRD_NAME LIKE '%" + keyword + "%') ";
        }

        if (status != null && !status.equals("ALL")) {
            sql += " AND shipmentStatus = '" + status + "' ";
        }

        sql += " ORDER BY o.DELIVERY_DATE ";

        return em.createNativeQuery(sql, ShipmentListDTO.class)
                .getResultList();
    }
}
