package com.yeoun.production.repository;

import com.yeoun.production.dto.ProductionPlanListDTO;
import com.yeoun.production.entity.ProductionPlan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, String> {

    // 오늘 날짜 기준 마지막 PLAN_ID 찾기
    @Query(value = """
        SELECT PLAN_ID
        FROM PRODUCTION_PLAN
        WHERE PLAN_ID LIKE :prefix || '%'
        ORDER BY PLAN_ID DESC
        FETCH FIRST 1 ROWS ONLY
        """, nativeQuery = true)
    String findLastPlanId(String prefix);
    
    List<ProductionPlan> findAllByOrderByCreatedAtDesc();
    
    
    /* 생산계획 리스트 조회 */
    @Query(value = """
        SELECT
            p.PLAN_ID        AS planId,
            p.CREATED_AT     AS createdAt,
            MIN(pr.PRD_NAME) AS itemName,   
            SUM(i.PLAN_QTY)  AS totalQty,   
            p.STATUS         AS status
        FROM PRODUCTION_PLAN p
        LEFT JOIN PRODUCTION_PLAN_ITEM i 
          ON p.PLAN_ID = i.PLAN_ID
        LEFT JOIN PRODUCT_MST pr
          ON i.PRD_ID = pr.PRD_ID
        GROUP BY 
            p.PLAN_ID,
            p.CREATED_AT,
            p.STATUS
        ORDER BY p.CREATED_AT DESC
    """, nativeQuery = true)
    List<ProductionPlanListDTO> findPlanList();



}




