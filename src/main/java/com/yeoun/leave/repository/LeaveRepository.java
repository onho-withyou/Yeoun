package com.yeoun.leave.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Emp;
import com.yeoun.leave.entity.AnnualLeave;

@Repository
public interface LeaveRepository extends JpaRepository<AnnualLeave, Long> {

	// 직원의 연차가 이미 존재하는지 확인
	boolean existsByEmp(Emp emp);

	// 개인 연차 조회
	Optional<AnnualLeave> findByEmp_EmpId(String empId);

}
