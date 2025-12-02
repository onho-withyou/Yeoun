package com.yeoun.pay.repository;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.enums.ActiveStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PayRuleRepository extends JpaRepository<PayRule, Long> {

    // [옵션 필터] 상태/시작일 구간을 한 번에 처리(파라미터가 null이면 무시)
    @Query("""
    select p from PayRule p
    where (:status is null or p.status = :status)
      and (:startFrom is null or p.startDate >= :startFrom)
      and (:startTo   is null or p.startDate <= :startTo)
    order by p.startDate desc, p.id desc
    """)
    List<PayRule> search(ActiveStatus status, LocalDate startFrom, LocalDate startTo);

    // [겹침 여부] (endDate null은 무기한으로 처리)
    @Query("""
    select (count(p) > 0) from PayRule p
    where (:excludeId is null or p.id <> :excludeId)
      and p.startDate <= coalesce(:newEnd, :maxDate)
      and coalesce(p.endDate, :maxDate) >= :newStart
    """)
    boolean existsOverlapping(@Param("newStart")  LocalDate newStart,
					          @Param("newEnd")    LocalDate newEnd,
					          @Param("maxDate")   LocalDate maxDate,
					          @Param("excludeId") Long excludeId);
    
    /** 
     *  활성 상태이면서 현재 기준일(asOf)이 유효기간(start~end)에 포함되는 규칙만 조회
     */
    @Query("""
    select p
      from PayRule p
     where p.status = :status
       and p.startDate <= :asOf
       and (p.endDate is null or p.endDate >= :asOf)
     order by p.startDate desc, p.id desc
    """)
    List<PayRule> findActiveValidRules(@Param("status") ActiveStatus status,
                                       @Param("asOf") LocalDate asOf);
}
