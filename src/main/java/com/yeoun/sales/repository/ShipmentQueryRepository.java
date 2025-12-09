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

        StringBuilder sql = new StringBuilder("""
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

                /* 출하상태 계산 (ENUM: WAITING, RESERVED, LACK, SHIPPED) */
                CASE 
                    WHEN EXISTS (
                        SELECT 1 FROM SHIPMENT s 
                        WHERE s.ORDER_ID = oi.ORDER_ID
                          AND s.SHIPMENT_STATUS = 'SHIPPED'
                    )
                        THEN 'SHIPPED'

                    WHEN EXISTS (
                        SELECT 1 FROM SHIPMENT s 
                        WHERE s.ORDER_ID = oi.ORDER_ID
                          AND s.SHIPMENT_STATUS = 'RESERVED'
                    )
                        THEN 'RESERVED'

                    WHEN NVL((
                        SELECT SUM(iv.IV_AMOUNT - iv.EXPECT_OB_AMOUNT)
                        FROM INVENTORY iv WHERE iv.ITEM_ID = oi.PRD_ID
                    ), 0) < oi.ORDER_QTY
                        THEN 'LACK'

                    ELSE 'WAITING'
                END AS status

            FROM ORDER_ITEM oi
            JOIN ORDERS o       ON oi.ORDER_ID = o.ORDER_ID
            JOIN PRODUCT_MST p  ON oi.PRD_ID = p.PRD_ID
            JOIN CLIENT c       ON o.CLIENT_ID = c.CLIENT_ID
            WHERE 1=1
        """);

        // ==============================
        // 날짜 조건
        // ==============================
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND o.DELIVERY_DATE >= TO_DATE(:startDate, 'YYYY-MM-DD') ");
        }

        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND o.DELIVERY_DATE <= TO_DATE(:endDate, 'YYYY-MM-DD') ");
        }

        // ==============================
        // 키워드 검색 조건
        // ==============================
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("""
                AND (
                    oi.ORDER_ID LIKE '%' || :keyword || '%'
                    OR c.CLIENT_NAME LIKE '%' || :keyword || '%'
                    OR p.PRD_NAME LIKE '%' || :keyword || '%'
                )
            """);
        }

        // ==============================
        // 상태 필터 조건
        // ==============================
        if (status != null && !status.equals("ALL")) {

            sql.append("""
                AND (
                    CASE 
                        WHEN EXISTS (
                            SELECT 1 FROM SHIPMENT s 
                            WHERE s.ORDER_ID = oi.ORDER_ID
                              AND s.SHIPMENT_STATUS = 'SHIPPED'
                        )
                            THEN 'SHIPPED'

                        WHEN EXISTS (
                            SELECT 1 FROM SHIPMENT s 
                            WHERE s.ORDER_ID = oi.ORDER_ID
                              AND s.SHIPMENT_STATUS = 'RESERVED'
                        )
                            THEN 'RESERVED'

                        WHEN NVL((
                            SELECT SUM(iv.IV_AMOUNT - iv.EXPECT_OB_AMOUNT)
                            FROM INVENTORY iv WHERE iv.ITEM_ID = oi.PRD_ID
                        ), 0) < oi.ORDER_QTY
                            THEN 'LACK'

                        ELSE 'WAITING'
                    END
                ) = :status
            """);
        }

        sql.append(" ORDER BY o.DELIVERY_DATE ");

        // ==============================
        // 쿼리 생성 + 파라미터 바인딩
        // ==============================
        var query = em.createNativeQuery(sql.toString(), ShipmentListDTO.class);

        if (startDate != null && !startDate.isEmpty()) query.setParameter("startDate", startDate);
        if (endDate != null && !endDate.isEmpty()) query.setParameter("endDate", endDate);
        if (keyword != null && !keyword.isEmpty()) query.setParameter("keyword", keyword);
        if (status != null && !status.equals("ALL")) query.setParameter("status", status);

        return query.getResultList();
    }
}
