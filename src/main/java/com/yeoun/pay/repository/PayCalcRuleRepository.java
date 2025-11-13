package com.yeoun.pay.repository;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PayCalcRuleRepository extends JpaRepository<PayCalcRule, Long> {

    // 항목별 목록 (우선순위 asc)
    List<PayCalcRule> findByItem_ItemCodeOrderByPriorityAsc(String itemCode);

    // 항목 + 대상 조건 목록 (우선순위 asc)
    List<PayCalcRule> findByItem_ItemCodeAndTargetTypeAndTargetCodeOrderByPriorityAsc(
            String itemCode, TargetType targetType, String targetCode);

    // 기간 중복 체크 (JPQL 그대로 사용 가능)
    @Query("""
        select r
          from PayCalcRule r
         where r.item.itemCode = :itemCode
           and (:targetType is null or r.targetType = :targetType)
           and coalesce(r.targetCode, '') = :targetCode
        """)
    List<PayCalcRule> findForOverlapCheck(@Param("itemCode") String itemCode,
                                          @Param("targetType") TargetType targetType,
                                          @Param("targetCode") String targetCode);

    // 전체 (우선순위 asc)
    List<PayCalcRule> findAllByOrderByPriorityAsc();

    // 📌 [핵심 교체] 기준일에 유효한 규칙 조회 (네이티브 쿼리: 컬럼/테이블 이름 기준 → 엔티티 필드명과 무관)
    @Query(value = """
        SELECT *
          FROM PAY_CALC_RULE R
         WHERE R.ITEM_CODE = :itemCode
           AND :asOf BETWEEN R.EFF_BEGIN_DATE AND NVL(R.EFF_END_DATE, :asOf)
         ORDER BY R.PRIORITY NULLS LAST, R.RULE_ID
        """, nativeQuery = true)
    List<PayCalcRule> findActiveByItemAndDate(@Param("itemCode") String itemCode,
                                              @Param("asOf") LocalDate asOf);
}
