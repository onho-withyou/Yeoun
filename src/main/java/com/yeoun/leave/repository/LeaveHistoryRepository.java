package com.yeoun.leave.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.leave.entity.AnnualLeaveHistory;

@Repository
public interface LeaveHistoryRepository extends JpaRepository<AnnualLeaveHistory, Long> {

	// 개인 연차 현황(리스트)
	List<AnnualLeaveHistory> findByEmp_EmpIdAndStartDateBetween(String empId, LocalDate startOfYear, LocalDate endOfYear);
}
