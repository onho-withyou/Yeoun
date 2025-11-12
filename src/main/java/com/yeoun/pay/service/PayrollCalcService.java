package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.repository.PayRuleRepository;
import com.yeoun.pay.repository.PayrollPayslipRepository;

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
    private final EmployeeQueryPort employeePort; // 어댑터가 구현해서 주입

    /** 외부(인사)조회 포트 */
    public static interface EmployeeQueryPort {
        List<SimpleEmp> findActiveEmployees();
    }
    /** 급여에 필요한 최소 필드 */
    public record SimpleEmp(String empId, String deptId, BigDecimal baseSalary) {}

    /** 가계산(시뮬레이션) */
    @Transactional
    public int simulateMonthly(String yyyymm, boolean overwrite) {
        return runMonthlyBatch(yyyymm, overwrite, null, true);
    }

    /** 확정 */
    @Transactional
    public int confirmMonthly(String yyyymm, boolean overwrite) {
        int cnt = runMonthlyBatch(yyyymm, overwrite, null, false);
        // 상태를 CONFIRMED 로 세팅
        List<PayrollPayslip> slips = payslipRepo.findByPayYymm(yyyymm);
        for (PayrollPayslip s : slips) {
        	 s.setStatus(CalcStatus.CONFIRMED);
        }
        payslipRepo.saveAll(slips);
        return cnt;
    }

    /** 월 일괄 계산 */
    @Transactional
    public int runMonthlyBatch(String payYymm, boolean overwrite, Long jobId, boolean simulated) {
        final String calcType = simulated ? "SIMULATED" : "CALCULATED";

        // 기준/항목/규칙 로딩
        List<PayRule> rules     = payRuleRepo.findAll();
        List<PayItemMst> items  = itemRepo.findAll();
        List<PayCalcRule> crules= calcRuleRepo.findAll();

        // ★★★ NPE 방지: null → 빈 리스트로
        List<SimpleEmp> employees =
                Optional.ofNullable(employeePort.findActiveEmployees())
                        .orElse(Collections.emptyList());

        if (employees.isEmpty()) {
            log.warn("runMonthlyBatch: active employee list is empty. yyyymm={}, simulated={}", payYymm, simulated);
            return 0; // 정책에 맞게 0건 처리
        }

        int count = 0;
        for (SimpleEmp emp : employees) {

            if (!overwrite && payslipRepo.existsByPayYymmAndEmpId(payYymm, emp.empId())) {
                log.info("SKIP exists payslip: {} {}", payYymm, emp.empId());
                continue;
            }

            BigDecimal baseAmt = calcBase(emp, rules, items, crules);
            BigDecimal alwAmt  = calcAllowances(emp, rules, items, crules, baseAmt);
            BigDecimal dedAmt  = calcDeductions(emp, rules, items, crules, baseAmt, alwAmt);
            BigDecimal totAmt  = baseAmt.add(alwAmt);
            BigDecimal netAmt  = totAmt.subtract(dedAmt);

            PayrollPayslip slip = payslipRepo.findByPayYymmAndEmpId(payYymm, emp.empId())
                    .orElseGet(PayrollPayslip::new);

            slip.setPayYymm(payYymm);
            slip.setEmpId(emp.empId());
            slip.setDeptId(emp.deptId());
            slip.setBaseAmt(safe(baseAmt));
            slip.setAlwAmt(safe(alwAmt));
            slip.setDedAmt(safe(dedAmt));
            slip.setTotAmt(safe(totAmt));
            slip.setNetAmt(safe(netAmt));
            slip.setCalcType("BATCH ALL");
            slip.setStatus(CalcStatus.CALCULATED);
            slip.setJobId(jobId);

            payslipRepo.save(slip);
            count++;
        }
        return count;
    }

    /* ================= 세부 계산 ================= */

    private BigDecimal calcBase(SimpleEmp emp, List<PayRule> rules, List<PayItemMst> items, List<PayCalcRule> crules) {
        return n(emp.baseSalary()); // 최소동작: 사원 기본급
    }

    private BigDecimal calcAllowances(SimpleEmp emp, List<PayRule> rules, List<PayItemMst> items,
                                      List<PayCalcRule> crules, BigDecimal baseAmt) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PayCalcRule r : crules) {
            if (!"ALLOWANCE".equalsIgnoreCase(r.getRuleType().name())) continue;
            BigDecimal val = switch (r.getCalcMethod()) {
                case FIXED    -> n(r.getAmount());
                case RATE     -> baseAmt.multiply(n(r.getRate()))
                                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                case FORMULA  -> formula(r.getExpr(), baseAmt, sum, BigDecimal.ZERO);
                case EXTERNAL -> externalAmount(emp, r, baseAmt, sum, BigDecimal.ZERO);
            };
            sum = sum.add(val);
        }
        return sum;
    }

    private BigDecimal calcDeductions(SimpleEmp emp, List<PayRule> rules, List<PayItemMst> items,
                                      List<PayCalcRule> crules, BigDecimal baseAmt, BigDecimal alwAmt) {

        BigDecimal sum   = BigDecimal.ZERO;
        BigDecimal total = baseAmt.add(alwAmt);

        for (PayCalcRule r : crules) {
            if (!"DEDUCTION".equalsIgnoreCase(r.getRuleType().name())) continue;

            BigDecimal val = switch (r.getCalcMethod()) {
                case FIXED    -> n(r.getAmount());
                case RATE     -> total.multiply(n(r.getRate()))
                                      .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                case FORMULA  -> formula(r.getExpr(), baseAmt, alwAmt, sum);
                case EXTERNAL -> externalAmount(emp, r, baseAmt, alwAmt, sum);
            };

            String item = r.getItem().getItemCode();
            if ("NPS".equalsIgnoreCase(item) || "HEALTH".equalsIgnoreCase(item) || "EI".equalsIgnoreCase(item)) {
                val = val.setScale(0, RoundingMode.HALF_UP); // 원단위 반올림
            }
            sum = sum.add(val);
        }
        return sum;
    }

    private BigDecimal externalAmount(SimpleEmp emp, PayCalcRule r,
                                      BigDecimal baseAmt, BigDecimal alwAmt, BigDecimal dedSum) {
        return BigDecimal.ZERO; // TODO 연동시 교체
    }

    private BigDecimal formula(String expr, BigDecimal base, BigDecimal alw, BigDecimal ded) {
        if (expr == null || expr.isBlank()) return BigDecimal.ZERO;
        String s = expr.toUpperCase()
                       .replace("BASE", base.toPlainString())
                       .replace("ALW",  alw.toPlainString())
                       .replace("DED",  ded.toPlainString())
                       .replace("%", "*0.01");
        try {
            return new java.math.BigDecimal(
                    new javax.script.ScriptEngineManager()
                            .getEngineByName("JavaScript").eval(s).toString()
            ).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("FORMULA eval error: {} -> 0", expr, e);
            return BigDecimal.ZERO;
        }
    }

    private static BigDecimal n(BigDecimal v){ return v==null?BigDecimal.ZERO:v; }
    private static BigDecimal safe(BigDecimal v){ return n(v).setScale(2, RoundingMode.HALF_UP); }

    public static String currentYymm() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
}
