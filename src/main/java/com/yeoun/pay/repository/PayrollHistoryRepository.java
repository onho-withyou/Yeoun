package com.yeoun.pay.repository;

import com.yeoun.pay.dto.PayrollHistoryProjection;
import com.yeoun.pay.entity.PayrollPayslip;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayrollHistoryRepository extends JpaRepository<PayrollPayslip, Long>  {

    /** 사원 검색 */
    @Query(value = """
        SELECT
            p.EMP_ID AS empId,
            e.EMP_NAME AS empName,
            d.DEPT_NAME AS deptName,
            p.PAY_YYMM AS payYymm,
            p.BASE_AMT AS baseAmt,
            p.ALW_AMT AS alwAmt,
            p.DED_AMT AS dedAmt,
            p.TOT_AMT AS totAmt,
            p.NET_AMT AS netAmt,
            p.CALC_STATUS AS calcStatus
        FROM PAYROLL_PAYSLIP p
          JOIN EMP e ON e.EMP_ID = p.EMP_ID
          LEFT JOIN DEPT d ON d.DEPT_ID = p.DEPT_ID
        WHERE (:keyword IS NULL
               OR e.EMP_ID LIKE '%' || :keyword || '%'
               OR e.EMP_NAME LIKE '%' || :keyword || '%')
          AND (:yymm IS NULL OR p.PAY_YYMM = :yymm)
        ORDER BY p.PAY_YYMM DESC, e.EMP_ID
        """, nativeQuery = true)
    List<PayrollHistoryProjection> searchByEmp(
            @Param("keyword") String keyword,
            @Param("yymm") String yymm);


    /** 부서 검색 */
    @Query(value = """
        SELECT
            p.EMP_ID AS empId,
            e.EMP_NAME AS empName,
            d.DEPT_NAME AS deptName,
            p.PAY_YYMM AS payYymm,
            p.BASE_AMT AS baseAmt,
            p.ALW_AMT AS alwAmt,
            p.DED_AMT AS dedAmt,
            p.TOT_AMT AS totAmt,
            p.NET_AMT AS netAmt,
            p.CALC_STATUS AS calcStatus
        FROM PAYROLL_PAYSLIP p
          JOIN EMP e ON e.EMP_ID = p.EMP_ID
          LEFT JOIN DEPT d ON d.DEPT_ID = p.DEPT_ID
        WHERE (:deptName IS NULL OR d.DEPT_NAME LIKE '%' || :deptName || '%')
          AND (:yymm IS NULL OR p.PAY_YYMM = :yymm)
        ORDER BY p.PAY_YYMM DESC, e.EMP_ID
        """, nativeQuery = true)
    List<PayrollHistoryProjection> searchByDept(
            @Param("deptName") String deptName,
            @Param("yymm") String yymm);


    /** 월별 검색 */
    @Query(value = """
        SELECT
            p.EMP_ID AS empId,
            e.EMP_NAME AS empName,
            d.DEPT_NAME AS deptName,
            p.PAY_YYMM AS payYymm,
            p.BASE_AMT AS baseAmt,
            p.ALW_AMT AS alwAmt,
            p.DED_AMT AS dedAmt,
            p.TOT_AMT AS totAmt,
            p.NET_AMT AS netAmt,
            p.CALC_STATUS AS calcStatus
        FROM PAYROLL_PAYSLIP p
          JOIN EMP e ON e.EMP_ID = p.EMP_ID
          LEFT JOIN DEPT d ON d.DEPT_ID = p.DEPT_ID
        WHERE p.PAY_YYMM = :yymm
        ORDER BY p.PAY_YYMM DESC, e.EMP_ID
        """, nativeQuery = true)
    List<PayrollHistoryProjection> searchByMonth(
            @Param("yymm") String yymm);
}
