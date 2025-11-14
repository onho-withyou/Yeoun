package com.yeoun.approval.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.approval.entity.ApprovalDoc;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Emp;

import java.util.List;
import java.util.Optional;


@Repository
public interface ApprovalDocRepository extends JpaRepository<ApprovalDoc, Long> {
	Optional<ApprovalDoc> findByEmpId(String empId);
	
	// 사원 목록 조회
	@Query("SELECT m FROM Emp m")
    List<Emp> findAllMember();
}
