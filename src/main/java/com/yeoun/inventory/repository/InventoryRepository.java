package com.yeoun.inventory.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
