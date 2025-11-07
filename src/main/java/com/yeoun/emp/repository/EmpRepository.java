package com.yeoun.emp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Emp;

@Repository
public interface EmpRepository extends JpaRepository<Emp, String> {
	
	// 사원번호 중복 확인
	boolean existsByEmpId(String empId);

}
