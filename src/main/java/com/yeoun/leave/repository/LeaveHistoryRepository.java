package com.yeoun.leave.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.leave.entity.AnnualLeaveHistory;

@Repository
public interface LeaveHistoryRepository extends JpaRepository<AnnualLeaveHistory, Long> {

	// 개인 연차 현황(리스트)
	@Query("""
			SELECT h
			FROM AnnualLeaveHistory h
			WHERE h.emp.empId = :empId
			  AND (
			        (h.startDate BETWEEN :startOfYear AND :endOfYear)
			     OR (h.endDate BETWEEN :startOfYear AND :endOfYear)
			  )
			""")
	 List<AnnualLeaveHistory> findAnnualLeaveInYear(
			 @Param("empId") String empId,
			 @Param("startOfYear") LocalDate startOfYear,
			 @Param("endOfYear") LocalDate endOfYear
		    );
}
