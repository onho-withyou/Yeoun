package com.yeoun.approval.repository;


import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.approval.entity.ApprovalDoc;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;

import java.util.List;
import java.util.Optional;


@Repository
public interface ApprovalDocRepository extends JpaRepository<ApprovalDoc, Long> {
	Optional<ApprovalDoc> findByEmpId(String empId);
	
	// 사원 목록 조회
	@Query("SELECT m FROM Emp m")
    List<Emp> findAllMember();
	
	//부서목록조회
	@Query("SELECT d FROM Dept d")
	List<Dept> findAllDepartments();


}
