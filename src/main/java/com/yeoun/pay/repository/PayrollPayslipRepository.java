package com.yeoun.pay.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.yeoun.pay.entity.PayrollPayslip;
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.dto.PayslipViewDTO;

public interface PayrollPayslipRepository extends JpaRepository<PayrollPayslip, Long> {

    /** 특정 사원의 특정월 명세 존재 여부 */
    boolean existsByPayYymmAndEmpId(String payYymm, String empId);

    /** 특정 사원의 특정월 명세 조회 */
    Optional<PayrollPayslip> findByPayYymmAndEmpId(String payYymm, String empId);

    /** ✅ 특정 월 전체 계산 여부 확인 */
    boolean existsByPayYymm(String payYymm);

    /** ✅ 특정 월 전체 건수 */
    long countByPayYymm(String payYymm);

    /** ✅ 특정 월 총 지급액 합계 */
    @Query("SELECT COALESCE(SUM(p.totAmt), 0) FROM PayrollPayslip p WHERE p.payYymm = :payYymm")
    BigDecimal sumTotalByYymm(@Param("payYymm") String payYymm);

    /** ✅ 특정 월 총 공제액 합계 */
    @Query("SELECT COALESCE(SUM(p.dedAmt), 0) FROM PayrollPayslip p WHERE p.payYymm = :payYymm")
    BigDecimal sumDeductByYymm(@Param("payYymm") String payYymm);

    /** ✅ 특정 월 총 실수령액 합계 */
    @Query("SELECT COALESCE(SUM(p.netAmt), 0) FROM PayrollPayslip p WHERE p.payYymm = :payYymm")
    BigDecimal sumNetByYymm(@Param("payYymm") String payYymm);

    /** ✅ 월 전체 확정 (확정자/확정일 포함) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PayrollPayslip p
           set p.calcStatus = :status,
               p.confirmUser = :userId,
               p.confirmDate = :now
         where p.payYymm = :payYymm
        """)
    int confirmMonth(@Param("payYymm") String payYymm,
                     @Param("status") CalcStatus status,
                     @Param("userId") String userId,
                     @Param("now") LocalDateTime now);

    /** ✅ 특정 사원만 확정 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PayrollPayslip p
           set p.calcStatus = :status,
               p.confirmUser = :userId,
               p.confirmDate = :now
         where p.payYymm = :payYymm
           and p.empId    = :empId
        """)
    int confirmOne(@Param("payYymm") String payYymm,
                   @Param("empId") String empId,
                   @Param("status") CalcStatus status,
                   @Param("userId") String userId,
                   @Param("now") LocalDateTime now);


    /* ✅ 사원이름 + 부서명 포함 전체 조회 (상태 구분 없이) */
    @Query(value = """
    	    SELECT
    	        p.PAYSLIP_ID AS payslipId,
    	        p.PAY_YYMM AS payYymm,
    	        p.EMP_ID AS empId,
    	        e.EMP_NAME AS empName,
    	        p.DEPT_ID AS deptId,
    	        d.DEPT_NAME AS deptName,
    	        p.BASE_AMT AS baseAmt,
    	        p.ALW_AMT AS alwAmt,
    	        p.DED_AMT AS dedAmt,
    	        p.NET_AMT AS netAmt,
    	        p.TOT_AMT AS totAmt,
    	        p.CALC_STATUS AS calcStatus
    	    FROM PAYROLL_PAYSLIP p
    	    LEFT JOIN EMP e ON e.EMP_ID = p.EMP_ID
    	    LEFT JOIN DEPT d ON d.DEPT_ID = p.DEPT_ID
    	    WHERE p.PAY_YYMM = :payYymm
    	      AND (:status IS NULL OR p.CALC_STATUS = :status)
    	    ORDER BY e.EMP_ID
    	""", nativeQuery = true)
    	List<PayslipViewDTO> findPayslipsWithEmpAndDept(
    	        @Param("payYymm") String payYymm,
    	        @Param("status") String status);

    
    
    
}
