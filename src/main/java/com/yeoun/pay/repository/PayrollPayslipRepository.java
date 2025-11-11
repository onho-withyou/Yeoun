package com.yeoun.pay.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yeoun.pay.entity.PayrollPayslip;
import com.yeoun.pay.enums.CalcStatus;

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

	List<PayrollPayslip> findByPayYymm(String yyyymm);	

//	List<PayrollPayslip> findByPayYymmAndCalcStatusOrderByEmpIdAsc(String yyyymm, CalcStatus calculated);
	
	List<PayrollPayslip> findByPayYymmAndStatusOrderByEmpIdAsc(String payYymm, CalcStatus status);

}
