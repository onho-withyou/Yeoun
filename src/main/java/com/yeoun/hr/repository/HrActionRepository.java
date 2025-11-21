package com.yeoun.hr.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.hr.dto.HrActionDTO;
import com.yeoun.hr.entity.HrAction;

@Repository
public interface HrActionRepository extends JpaRepository<HrAction, Long> {
	
	// 인사 발령 목록
	List<HrAction> findAll();
	
	// 인사 발령 목록 (검색 + 페이징)
    @Query("""
        select a
        from HrAction a
          join a.emp e
        where
    	  a.status = '완료'
    	  and
          (:keyword is null or :keyword = '' 
            or e.empId like concat('%', :keyword, '%')
            or e.empName like concat('%', :keyword, '%'))
          and (:actionType is null or :actionType = '' or a.actionType = :actionType)
          and (:startDate is null or a.effectiveDate >= :startDate)
          and (:endDate is null or a.effectiveDate <= :endDate)
        """)
    Page<HrAction> searchHrActions(@Param("keyword") String keyword,
                                   @Param("actionType") String actionType,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   Pageable pageable);

    // 전자결재 연결
    Optional<HrAction> findByApprovalId(Long approvalId);


}
