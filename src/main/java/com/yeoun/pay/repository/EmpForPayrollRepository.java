package com.yeoun.pay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.yeoun.emp.entity.Emp;



public interface EmpForPayrollRepository extends JpaRepository<Emp, String> {

 interface Row {
     String getEmpId();
     String getDeptId();
     String getPosCode();
 }

 @Query(value = """
     SELECT  e.EMP_ID   AS empId,
             em.DEPT_ID AS deptId,
             em.POS_CODE AS posCode
       FROM  EMP e
       JOIN  EMPLOYMENT em
             ON em.EMP_ID = e.EMP_ID
            AND (em.END_DATE IS NULL)
       JOIN  DEPT d
             ON d.DEPT_ID = em.DEPT_ID
      WHERE  e.STATUS = 'ACTIVE'
        AND  d.USE_YN = 'Y'
     """, nativeQuery = true)
 List<Row> findActiveForPayroll();
}
