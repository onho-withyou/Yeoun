package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.entity.PayrollPayslip;
import com.yeoun.pay.enums.ActiveStatus;
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.repository.PayRuleRepository;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.yeoun.pay.enums.CalcMethod.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class PayrollCalcService {

    private final PayrollPayslipRepository payslipRepo;
    private final PayRuleRepository payRuleRepo;
    private final PayItemMstRepository itemRepo;
    private final PayCalcRuleRepository calcRuleRepo;
    private final EmployeeQueryPort employeePort;

    @PersistenceContext
    private EntityManager em;

    /** 외부(인사)조회 포트 */
    public static interface EmployeeQueryPort {
        List<SimpleEmp> findActiveEmployees();
    }

    /** 급여에 필요한 최소 필드 */
    public record SimpleEmp(String empId, String deptId, BigDecimal baseSalary) {}

    /* ========================= 시뮬레이션 ========================= */
    @Transactional
    public int simulateMonthly(String yyyymm, boolean overwrite) {
        log.info("[SIMULATE] {}월 급여 시뮬레이션 시작 (overwrite={})", yyyymm, overwrite);
        int cnt = runMonthlyBatch(yyyymm, overwrite, null, true);
        log.info("[SIMULATE] {}월 시뮬레이션 완료: {}건 반영", yyyymm, cnt);
        return cnt;
    }
    
    

    /* ========================= 월 확정(전체) ========================= */
    @Transactional
    public int confirmMonthly(String yyyymm, boolean overwrite, String userId) {
        log.info("[CONFIRM] {}월 급여 확정 처리 시작 (overwrite={})", yyyymm, overwrite);

        int calcCnt = runMonthlyBatch(yyyymm, overwrite, null, false);
        int upd = payslipRepo.confirmMonth(yyyymm, CalcStatus.CONFIRMED, optUser(userId), LocalDateTime.now());
        log.info("[CONFIRM] {}월 확정 완료: 계산 {}건, 확정 {}건", yyyymm, calcCnt, upd);
        return upd;
    }

    /* ========================= 공통 월 일괄 계산 ========================= */
    @Transactional
    public int runMonthlyBatch(String payYymm, boolean overwrite, Long jobId, boolean simulated) {
        final CalcStatus status = simulated ? CalcStatus.SIMULATED : CalcStatus.CALCULATED;
        log.info("[{}] 급여 일괄 계산 시작 (overwrite={}, simulated={})", payYymm, overwrite, simulated);

        // 1. 규칙 조회
        log.info("단계1: 규칙 조회 시작");
        List<PayRule> rules = payRuleRepo.findActiveValidRules(ActiveStatus.ACTIVE, LocalDate.now());
        List<PayItemMst> items  = itemRepo.findAll();
        List<PayCalcRule> crules = calcRuleRepo.findAll();
    log.info("단계2: 규칙 조회 완료 (rules={}, items={}, calcRules={})", rules.size(), items.size(), crules.size());

        // 2. 활성 사원 조회
        List<SimpleEmp> employees =
                Optional.ofNullable(employeePort.findActiveEmployees()).orElse(Collections.emptyList());
        log.info("단계3: 활성 사원 {}명 조회", employees.size());
        if (employees.isEmpty()) {
            log.warn("[{}] 활성 사원 없음 → 계산 중단", payYymm);
            return 0;
        }

        int successCount = 0;

        // 3. 사원별 급여 계산
        for (SimpleEmp emp : employees) {
            try {
                log.info("단계4: [{}] 사번 계산 시작", emp.empId());

                // overwrite가 false면 기존 건 스킵
                if (!overwrite && payslipRepo.existsByPayYymmAndEmpId(payYymm, emp.empId())) {
                    log.debug("SKIP: {} / {}", payYymm, emp.empId());
                    continue;
                }

                BigDecimal baseAmt = calcBase(emp, rules, items, crules);
                BigDecimal alwAmt  = calcAllowances(emp, rules, items, crules, baseAmt);
                BigDecimal dedAmt  = calcDeductions(emp, rules, items, crules, baseAmt, alwAmt);
                BigDecimal totAmt  = baseAmt.add(alwAmt);
                BigDecimal netAmt  = totAmt.subtract(dedAmt);

                // 🔸 기존 명세 존재 여부 체크
                PayrollPayslip existing = payslipRepo.findByPayYymmAndEmpId(payYymm, emp.empId()).orElse(null);
                boolean isNew = (existing == null);

                PayrollPayslip slip = isNew ? new PayrollPayslip() : existing;

                // 필드 설정
                slip.setPayYymm(payYymm);
                slip.setEmpId(emp.empId());
                slip.setDeptId(emp.deptId());
                slip.setBaseAmt(safe(baseAmt));
                slip.setAlwAmt(safe(alwAmt));
                slip.setDedAmt(safe(dedAmt));
                slip.setTotAmt(safe(totAmt));
                slip.setNetAmt(safe(netAmt));
                slip.setCalcType(simulated ? "SIMULATED" : "BATCH ALL");
                slip.setCalcStatus(status);
                slip.setJobId(jobId);
                slip.setCalcDt(LocalDateTime.now());

                // 신규일 경우 insert
                if (isNew) {
                    log.info("✅ 신규 insert 시도: empId={}, payYymm={}", emp.empId(), payYymm);
                    em.persist(slip);
                    em.flush();
                    log.info("✅ insert 완료 → payslipId={}", slip.getPayslipId());
                } else {
                    log.info("▶ 기존 update 시도: empId={}, payslipId={}", emp.empId(), slip.getPayslipId());
                    em.merge(slip);
                    em.flush();
                    log.info("▶ update 완료");
                }

                successCount++;

            } catch (Exception e) {
                log.error("❌ [{}] 급여 계산 실패: {}", emp.empId(), e.getMessage(), e);
            }
        }

        log.info("[{}] 급여계산 완료: 총 {}건 처리 (상태={})", payYymm, successCount, status);
        return successCount;
    }

    /* ========================= 계산 서브루틴 ========================= */

    /** [1] 기본급 계산: PAY_RULE.BASE_AMT */
    private BigDecimal calcBase(SimpleEmp emp, List<PayRule> rules, List<PayItemMst> items, List<PayCalcRule> crules) {
        PayRule rule = rules.stream().findFirst().orElse(null);
        if (rule == null) return BigDecimal.ZERO;

        BigDecimal base = n(rule.getBaseAmt());
        log.info("▶ 기본급 조회: empId={}, baseAmt={}", emp.empId(), base);
        return base.setScale(2, RoundingMode.HALF_UP);
    }


    /** [2] 수당 계산: PAY_RULE.MEAL_AMT, TRANS_AMT 포함 */
    private BigDecimal calcAllowances(SimpleEmp emp, List<PayRule> rules, List<PayItemMst> items,
                                      List<PayCalcRule> crules, BigDecimal baseAmt) {

        PayRule rule = rules.stream().findFirst().orElse(null);
        if (rule == null) return BigDecimal.ZERO;

        // DB의 Double 컬럼을 BigDecimal로 변환
        BigDecimal meal = BigDecimal.valueOf(Optional.ofNullable(rule.getMealAmt()).orElse(0.0));
        BigDecimal trans = BigDecimal.valueOf(Optional.ofNullable(rule.getTransAmt()).orElse(0.0));

        BigDecimal totalAllow = meal.add(trans);
        log.info("▶ 수당 계산: empId={}, meal={}, trans={}, totalAllow={}", emp.empId(), meal, trans, totalAllow);

        return totalAllow.setScale(2, RoundingMode.HALF_UP);
    }


    /** [3] 공제 계산: PAY_RULE.PEN_RATE, HLTH_RATE, EMP_RATE, TAX_RATE 기준 */
    private BigDecimal calcDeductions(SimpleEmp emp, List<PayRule> rules, List<PayItemMst> items,
                                      List<PayCalcRule> crules, BigDecimal baseAmt, BigDecimal alwAmt) {

        PayRule rule = rules.stream().findFirst().orElse(null);
        if (rule == null) return BigDecimal.ZERO;

        BigDecimal total = baseAmt.add(alwAmt);

        // Double → BigDecimal 변환
        BigDecimal penRate  = BigDecimal.valueOf(Optional.ofNullable(rule.getPenRate()).orElse(0.0));
        BigDecimal hlthRate = BigDecimal.valueOf(Optional.ofNullable(rule.getHlthRate()).orElse(0.0));
        BigDecimal empRate  = BigDecimal.valueOf(Optional.ofNullable(rule.getEmpRate()).orElse(0.0));
        BigDecimal taxRate  = BigDecimal.valueOf(Optional.ofNullable(rule.getTaxRate()).orElse(0.0));

        // 총 공제 = (총지급액 × 각 요율)
        BigDecimal penDed  = total.multiply(penRate);
        BigDecimal hlthDed = total.multiply(hlthRate);
        BigDecimal empDed  = total.multiply(empRate);
        BigDecimal taxDed  = total.multiply(taxRate);

        BigDecimal totalDed = penDed.add(hlthDed).add(empDed).add(taxDed);

        log.info("▶ 공제 계산: empId={}, total={}, 연금={}, 건보={}, 고용={}, 소득세={}, totalDed={}",
                emp.empId(), total, penDed, hlthDed, empDed, taxDed, totalDed);

        return totalDed.setScale(2, RoundingMode.HALF_UP);
    }
    
    
 // ========================= 유틸 메서드 ========================= //
    /** BigDecimal null-safe 변환 */
    private static BigDecimal n(BigDecimal v) {
        return (v == null) ? BigDecimal.ZERO : v;
    }

    /** 금액 반올림 (소수점 2자리) */
    private static BigDecimal safe(BigDecimal v) {
        return n(v).setScale(2, RoundingMode.HALF_UP);
    }

    /** 사용자 ID 기본값 처리 */
    private static String optUser(String userId) {
        return (userId == null || userId.isBlank()) ? "SYSTEM" : userId;
    }

    /** 현재 년월(yyyyMM) 반환 */
    public static String currentYymm() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

	public int simulateMonthly(String yyyymm, String calcType, boolean overwrite) {
		// TODO Auto-generated method stub
		return 0;
	}



}