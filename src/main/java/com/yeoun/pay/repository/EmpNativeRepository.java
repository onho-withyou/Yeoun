package com.yeoun.pay.repository;

import com.yeoun.emp.entity.Emp;
import com.yeoun.pay.dto.EmpForPayrollProjection;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmpNativeRepository extends JpaRepository<Emp, String> {
  
	/** 활성화 부서,사원 조회*/
	 @Query(value = """
		        SELECT
		            e.EMP_ID      AS empId,
		            e.EMP_NAME    AS empName,
		            e.ROLE_CODE   AS roleCode,
		            e.STATUS      AS status,
		            e.HIRE_DATE   AS hireDate,
		            e.DEPT_ID     AS deptId
		        FROM EMP e
		        JOIN DEPT d
		          ON d.DEPT_ID = e.DEPT_ID
		         AND d.USE_YN = 'Y'
		        WHERE e.STATUS = 'ACTIVE'
		        ORDER BY e.EMP_ID
		        """, nativeQuery = true)
		    List<EmpForPayrollProjection> findActiveEmpForPayroll();
				


    /** 사원명 조회 */
    @Query(value="""
    		SELECT e.empName 
              FROM Emp e 
              WHERE e.empId = :empId
              """)
    Optional<String> findEmpNameById(@Param("empId") String empId);


    /** ⬅️ 부서명 조회 */
    @Query(value = """
        SELECT d.DEPT_NAME
          FROM EMP e
          JOIN DEPT d ON d.DEPT_ID = e.DEPT_ID
         WHERE e.EMP_ID = :empId
        """, nativeQuery = true)
    Optional<String> findDeptNameById(@Param("empId") String empId);



    /** 확정사원명 조회*/
    @Query(value = """
            SELECT E.EMP_NAME 
            FROM EMP E 
            WHERE E.EMP_ID = :empId
            """, nativeQuery = true)
    String findEmpNameByEmpId(@Param("empId") String empId);
    
    
    /**직급 조회*/
    @Query(value = """
    	    SELECT e.POS_CODE 
    	    FROM EMP e 
    	    WHERE e.EMP_ID = :empId
    	""", nativeQuery = true)
    	Optional<String> findEmpPositionById(@Param("empId") String empId);

    /**사용연차 조회*/
    	@Query(value = """
    	    SELECT NVL(a.remain_days, 0)
    	    FROM ANNUAL_LEAVE a
    	    WHERE a.EMP_ID = :empId
    	""", nativeQuery = true)
    	Optional<Integer> findUsedAnnualById(@Param("empId") String empId);

    	/** 특정 연도의 잔여 연차(remain_days) 조회 */
    	@Query(value = """
    	    SELECT NVL(a.remain_days, 0)
    	      FROM ANNUAL_LEAVE a
    	     WHERE a.EMP_ID = :empId
    	       AND a.use_year = :year
    	    """, nativeQuery = true)
    	Optional<Integer> findRemainDaysByYear(
    	        @Param("empId") String empId,
    	        @Param("year") int year);


}
