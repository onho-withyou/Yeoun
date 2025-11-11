package com.yeoun.pay.repository;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayCalcRuleRepository extends JpaRepository<PayCalcRule, Long> {

    // 항목별 목록 (우선순위 asc)
    List<PayCalcRule> findByItem_ItemCodeOrderByPriorityAsc(String itemCode);

    // 항목 + 대상 조건 목록 (우선순위 asc)
    List<PayCalcRule> findByItem_ItemCodeAndTargetTypeAndTargetCodeOrderByPriorityAsc(
            String itemCode, TargetType targetType, String targetCode);

    // 기간 중복 체크용 (대상 null/빈문자 논리 포함)
    @Query("""
        select r
          from PayCalcRule r
         where r.item.itemCode = :itemCode          
          
           and (:targetType IS NULL OR r.targetType = :targetType)           
         
           and coalesce(r.targetCode, '') = :targetCode
        """)
    List<PayCalcRule> findForOverlapCheck(@Param("itemCode") String itemCode,
                                          @Param("targetType") TargetType targetType,
                                          @Param("targetCode") String targetCode);

    
    List<PayCalcRule> findAllByOrderByPriorityAsc();
}