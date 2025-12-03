package com.yeoun.emp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.EmpBank;

@Repository
public interface EmpBankRepository extends JpaRepository<EmpBank, Long> {
	
	// 사원번호로 계좌정보 조회
	Optional<EmpBank> findByEmpId(String empId);

	// 상세 조회에 사용
	Optional<EmpBank> findTopByEmpIdOrderByCreatedDateDesc(String empId);


}
