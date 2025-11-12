package com.yeoun.pay.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    /** ✅ 상태별 조회 */
    List<PayrollPayslip> findByPayYymmAndCalcStatus(String payYymm, CalcStatus status);

    /** ✅ 상태별 조회 (정렬 포함) */
    List<PayrollPayslip> findByPayYymmAndCalcStatusOrderByEmpIdAsc(String yyyymm, CalcStatus calculated);

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


    /* ✅ 사원이름 + 부서명 포함 조회용 (DTO 매핑) */
    @Query("""
    	    SELECT new com.yeoun.pay.dto.PayslipViewDTO(
    	        p.payslipId, p.payYymm, p.empId, e.empName, p.deptId, d.deptName,
    	        p.baseAmt, p.alwAmt, p.dedAmt, p.netAmt, p.calcStatus
    	    )
    	    FROM PayrollPayslip p
    	    LEFT JOIN Emp e ON e.empId = p.empId
    	    LEFT JOIN Dept d ON d.deptId = p.deptId
    	    WHERE p.payYymm = :payYymm
    	    ORDER BY e.empId
    	""")
    	List<PayslipViewDTO> findPayslipsWithEmpAndDept(@Param("payYymm") String payYymm);


}
