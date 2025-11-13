package com.yeoun.pay.repository;

import com.yeoun.emp.entity.Emp;
import com.yeoun.pay.dto.EmpForPayrollProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmpNativeRepository extends JpaRepository<Emp, String> {

    @Query(value = """
        SELECT
            e.EMP_ID    AS empId,
            e.DEPT_ID   AS deptId,
            TO_NUMBER(0) AS baseSalary
          FROM EMP e
          JOIN DEPT d
            ON d.DEPT_ID = e.DEPT_ID
           AND d.USE_YN = 'Y'
         WHERE (:status IS NULL OR e.STATUS = :status)
         ORDER BY e.EMP_ID
        """, nativeQuery = true)
    List<EmpForPayrollProjection> findEmpForPayrollByStatus(@Param("status") String status);

    @Query(value = """
        SELECT
            e.EMP_ID    AS empId,
            e.DEPT_ID   AS deptId,
            TO_NUMBER(0) AS baseSalary
          FROM EMP e
          JOIN DEPT d
            ON d.DEPT_ID = e.DEPT_ID
           AND d.USE_YN = 'Y'
         WHERE (:status IS NULL OR e.STATUS = :status)
        """,
        countQuery = """
        SELECT COUNT(1)
          FROM EMP e
          JOIN DEPT d
            ON d.DEPT_ID = e.DEPT_ID
           AND d.USE_YN = 'Y'
         WHERE (:status IS NULL OR e.STATUS = :status)
        """,
        nativeQuery = true)
    Page<EmpForPayrollProjection> findEmpForPayrollByStatus(
            @Param("status") String status,
            Pageable pageable);


    /** 사원명 조회 */
    @Query("SELECT e.empName FROM Emp e WHERE e.empId = :empId")
    Optional<String> findEmpNameById(@Param("empId") String empId);


    /** ⬅️ 부서명 조회 (Native Query 사용) */
    @Query(value = """
        SELECT d.DEPT_NAME
          FROM EMP e
          JOIN DEPT d ON d.DEPT_ID = e.DEPT_ID
         WHERE e.EMP_ID = :empId
        """, nativeQuery = true)
    Optional<String> findDeptNameById(@Param("empId") String empId);
}
