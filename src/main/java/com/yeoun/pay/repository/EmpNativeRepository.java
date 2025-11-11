package com.yeoun.pay.repository;

import com.yeoun.emp.entity.Emp;
import com.yeoun.pay.dto.EmpForPayrollProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpNativeRepository extends JpaRepository<Emp, String> {

    @Query(value = """
        SELECT
            e.EMP_ID                AS empId,
            em.DEPT_ID              AS deptId,
            TO_NUMBER(0)         AS baseSalary
        FROM EMP e
        LEFT JOIN EMPLOYMENT em
               ON em.EMP_ID = e.EMP_ID
              AND em.END_DATE IS NULL
        WHERE (:status IS NULL OR e.STATUS = :status)
        ORDER BY e.EMP_ID
        """, nativeQuery = true)
    List<EmpForPayrollProjection> findEmpForPayrollByStatus(@Param("status") String status);

    @Query(value = """
        SELECT
            e.EMP_ID                AS empId,
            em.DEPT_ID              AS deptId,
            TO_NUMBER(0)         AS baseSalary
        FROM EMP e
        LEFT JOIN EMPLOYMENT em
               ON em.EMP_ID = e.EMP_ID
              AND em.END_DATE IS NULL
        WHERE (:status IS NULL OR e.STATUS = :status)
        """,
        countQuery = """
        SELECT COUNT(1)
          FROM EMP e
          LEFT JOIN EMPLOYMENT em
                 ON em.EMP_ID = e.EMP_ID
                AND em.END_DATE IS NULL
         WHERE (:status IS NULL OR e.STATUS = :status)
        """,
        nativeQuery = true)
    Page<EmpForPayrollProjection> findEmpForPayrollByStatus(@Param("status") String status,
                                                            Pageable pageable);
}
