package com.yeoun.production.repository;

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
}
