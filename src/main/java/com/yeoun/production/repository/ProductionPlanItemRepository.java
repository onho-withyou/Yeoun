package com.yeoun.production.repository;

import com.yeoun.production.entity.ProductionPlanItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductionPlanItemRepository extends JpaRepository<ProductionPlanItem, String> {

    @Query(value = """
        SELECT PLAN_ITEM_ID
        FROM PRODUCTION_PLAN_ITEM
        WHERE PLAN_ITEM_ID LIKE :prefix || '%'
        ORDER BY PLAN_ITEM_ID DESC
        FETCH FIRST 1 ROWS ONLY
        """, nativeQuery = true)
    String findLastPlanItemId(String prefix);
    
    
    
    List<ProductionPlanItem> findByPlanId(String planId);
}
