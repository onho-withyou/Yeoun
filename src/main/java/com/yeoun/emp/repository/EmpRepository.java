package com.yeoun.emp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Emp;

@Repository
public interface EmpRepository extends JpaRepository<Emp, String> {
	
	// 사원번호로 사용자 조회
	Optional<Emp> findByEmpId(String empId);
	
	// 사원번호 + 역할
	@Query("""
			  SELECT e
			  FROM Emp e
			    LEFT JOIN FETCH e.dept d
			    LEFT JOIN FETCH e.empRoles er
			    LEFT JOIN FETCH er.role r
			  WHERE e.empId = :empId
			""")
	Optional<Emp> findByEmpIdWithDeptAndRoles(@Param("empId") String empId);
	
	// 사원번호 중복 확인
	boolean existsByEmpId(String candidate);
	
//	// 이메일 중복 확인
//	boolean existsByEmail(String email);

}
