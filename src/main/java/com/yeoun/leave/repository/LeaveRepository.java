package com.yeoun.leave.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Emp;
import com.yeoun.leave.entity.AnnualLeave;

@Repository
public interface LeaveRepository extends JpaRepository<AnnualLeave, Long> {

	// 직원의 연차가 이미 존재하는지 확인
	boolean existsByEmp(Emp emp);

	// 개인 연차 조회
	Optional<AnnualLeave> findByEmp_EmpId(String empId);

	// 관리자용 연차 조회
	@Query("""
			SELECT al
			FROM AnnualLeave al
			JOIN FETCH al.emp e
			JOIN FETCH e.dept d
			JOIN FETCH e.position p
			WHERE (:empId IS NULL  
					OR :empId = ''
					OR e.empId LIKE CONCAT('%', :empId, '%'))
			ORDER BY e.empId ASC
			""")
	List<AnnualLeave> findAllWithEmpInfo(@Param("empId") String empId);

}
