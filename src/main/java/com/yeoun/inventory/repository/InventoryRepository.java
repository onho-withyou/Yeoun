package com.yeoun.inventory.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.dto.InventorySafetyCheckDTO;
import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.entity.WarehouseLocation;

public interface InventoryRepository
	extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

	Optional<Inventory> findByWarehouseLocationAndLotNo(WarehouseLocation location, String lotNo);
	
	// 생산계획시 필요한 제품(PRD_ID / ITEM_ID) 기준 전체 재고 조회

		@Query(value = """
		    SELECT 
		        ITEM_ID AS prdId,
		        SUM(IV_AMOUNT-EXPECT_OB_AMOUNT) AS currentStock
		    FROM INVENTORY		   
		    GROUP BY ITEM_ID
		""", nativeQuery = true)
		List<Map<String, Object>> findCurrentStockGrouped();		


	List<Inventory> findByWarehouseLocation(WarehouseLocation location);

	
	@Query(value = """
        SELECT
            ss.ITEM_ID                       AS itemId,
            ss.ITEM_NAME                     AS itemName,
            ss.ITEM_TYPE                     AS itemType,
            NVL(inv.IN_QTY, 0)               AS ivQty,
            NVL(inv.PLAN_OUT_QTY, 0)         AS planOutQty,
            NVL(inv.IN_QTY, 0)
              - NVL(inv.PLAN_OUT_QTY, 0)     AS expectIvQty,
            NVL(inv.LOCATIONS_CNT, 0)        AS locationsCnt,
            ss.SAFETY_STOCK_QTY              AS safetyStockQty,
            ss.SAFETY_STOCK_QTY_DAILY        AS safetyStockQtyDaily
        FROM SAFETY_STOCK ss
        LEFT JOIN (
            SELECT
                ITEM_ID,
                SUM(CASE 
                        WHEN IV_STATUS != 'EXPIRED' THEN IV_AMOUNT 
                        ELSE 0 
                    END) AS IN_QTY,
                SUM(CASE 
                        WHEN IV_STATUS != 'EXPIRED' THEN EXPECT_OB_AMOUNT 
                        ELSE 0 
                    END) AS PLAN_OUT_QTY,
                COUNT(DISTINCT LOCATION_ID) AS LOCATIONS_CNT
            FROM INVENTORY
            WHERE IV_STATUS != 'EXPIRED'
            GROUP BY ITEM_ID
        ) inv
            ON inv.ITEM_ID = ss.ITEM_ID
        ORDER BY ss.ITEM_ID
        """,
        nativeQuery = true)
	List<InventorySafetyCheckDTO> getIvSummaryWithSafetyStock();
	
	
	@Modifying
	@Query("""
	    UPDATE Inventory i 
	    SET i.ivStatus = CASE
		    WHEN i.expirationDate IS NULL THEN i.ivStatus
	        WHEN i.expirationDate < :today THEN 'EXPIRED'
	        WHEN i.expirationDate BETWEEN :today AND :disposalWaitDate THEN 'DISPOSAL_WAIT'
	        ELSE 'NORMAL'
	    END
	    WHERE i.expirationDate IS NOT NULL
			AND i.ivStatus IN ('NORMAL', 'DISPOSAL_WAIT')
	""")
	void updateAllStatusByExpirationDate(@Param("today") LocalDateTime today, 
		    @Param("disposalWaitDate") LocalDateTime disposalWaitDate);
	

}
