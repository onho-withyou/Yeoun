package com.yeoun.emp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Emp;

@Repository
public interface EmpRepository extends JpaRepository<Emp, String> {
	
	// 사원번호로 사용자 조회
	Optional<Emp> findByEmpId(String empId);
	
	// 사원번호 + 부서 + 역할
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
	
	// 사원 목록 조회
	@Query("""
	        select new com.yeoun.emp.dto.EmpListDTO(
	            e.hireDate,
	            e.empId,
	            e.empName,
	            d.deptName,
	            p.posName,
	            p.rankOrder,
	            e.mobile,
	            e.email
	        )
	        from Emp e
	          join e.dept d
	          join e.position p
	        where
	          e.status = 'ACTIVE'
      	      and ( :keyword is null or :keyword = '' or
	                e.empId    like concat('%', :keyword, '%') or
	                e.empName  like concat('%', :keyword, '%') or
	                d.deptName like concat('%', :keyword, '%') or
	                p.posName  like concat('%', :keyword, '%') or
	                e.email    like concat('%', :keyword, '%')
	          )
	          and ( :deptId is null or :deptId = '' or d.deptId = :deptId )
	        """)
	 Page<EmpListDTO> searchEmpList(@Param("keyword") String keyword,
             @Param("deptId") String deptId,
             Pageable pageable);
	
	
	// 인사 발령 등록 화면에서 사용되는 사원 목록
	@Query("""
		    SELECT e
		      FROM Emp e
		     WHERE (:deptId IS NULL OR e.dept.deptId = :deptId)
		       AND (:posCode IS NULL OR e.position.posCode = :posCode)
		       AND (:keyword IS NULL
		            OR e.empName LIKE %:keyword%
		            OR e.empId   LIKE %:keyword%)
		     ORDER BY e.hireDate DESC
		""")
	List<Emp> searchForHrAction(@Param("deptId") String deptId,
	                            @Param("posCode") String posCode,
	                            @Param("keyword") String keyword);

	
	

}
