package com.yeoun.pay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.yeoun.emp.entity.Emp;

public interface EmpForPayrollRepository extends JpaRepository<Emp, String> {

    interface Row {
        String getEmpId();
        String getEmpName();
        String getDeptId();
        String getDeptName();
        String getPosCode();
    }

    @Query(value = """
        SELECT  e.EMP_ID    AS empId,
                e.EMP_NAME  AS empName,
                e.DEPT_ID   AS deptId,
                d.DEPT_NAME AS deptName,
                e.POS_CODE  AS posCode
          FROM  EMP e
          JOIN  DEPT d
                ON d.DEPT_ID = e.DEPT_ID
         WHERE  e.STATUS = 'ACTIVE'
           AND  d.USE_YN = 'Y'
         ORDER BY e.EMP_ID
        """, nativeQuery = true)
    List<Row> findActiveForPayroll();
}
