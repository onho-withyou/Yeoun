package com.yeoun.masterData.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;

import com.yeoun.masterData.entity.SafetyStock;

public interface SafetyStockRepository extends JpaRepository<SafetyStock, String> {
    //안전재고 그리드 조회
    @Query(value = """
        SELECT s
        FROM SafetyStock s
        WHERE (:itemId IS NULL OR :itemId = '' OR s.itemId LIKE %:itemId%)
          AND (:itemName IS NULL OR :itemName = '' OR s.itemName LIKE %:itemName%)
        ORDER BY s.itemId ASC
        """, nativeQuery = false)
    List<SafetyStock> findByItemlList(@Param("itemId") String itemId, @Param("itemName")  String itemName);
}
