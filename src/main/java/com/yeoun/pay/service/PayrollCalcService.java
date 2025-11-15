package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.dto.PayslipDetailDTO;
import com.yeoun.pay.entity.EmpPayItem;
import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.entity.PayrollPayslip;
import com.yeoun.pay.enums.ActiveStatus;
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.repository.EmpPayItemRepository;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.repository.PayRuleRepository;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PayrollCalcService {

    private final PayrollPayslipRepository payslipRepo;
    private final PayRuleRepository payRuleRepo;
    private final PayItemMstRepository itemRepo;
    private final PayCalcRuleRepository calcRuleRepo;
    private final EmployeeQueryPort employeePort;
    private final EmpPayItemRepository empPayItemRepo;

    @PersistenceContext
    private EntityManager em;

    /* =========================전체 시뮬레이션 ========================= */
    @Transactional
    public int simulateMonthly(String yyyymm, boolean overwrite) {
        return runMonthlyBatch(yyyymm, overwrite, null, true , null);
    }
    
    /* ========================= 개별 시뮬레이션 ========================= */
    @Transactional
    public int simulateOne(String yyyymm, String empId, boolean overwrite) {
        return runMonthlyBatch(yyyymm, overwrite, null, true, empId);
    }

    /* =========================전체 확정 ========================= */
    @Transactional
    public int confirmMonthly(String yyyymm, boolean overwrite, String userId) {
        int calcCnt = runMonthlyBatch(yyyymm, overwrite, null, false ,null);
        payslipRepo.confirmMonth(yyyymm, CalcStatus.CONFIRMED, optUser(userId), LocalDateTime.now());
        return calcCnt;
    } 
    
    /* ========================= 개별 확정 ========================= */
    @Transactional
    public int confirmOne(String yyyymm, String empId, boolean overwrite, String userId) {
        int calcCnt = runMonthlyBatch(yyyymm, overwrite, null, false, empId);
        // 해당 사원만 확정 처리
        payslipRepo.confirmOne(yyyymm, empId, CalcStatus.CONFIRMED, optUser(userId), LocalDateTime.now());
        return calcCnt;
    }

    /* ========================= 공통 batch (전체/개별 공용) ========================= */
    @Transactional
    public int runMonthlyBatch(String payYymm, boolean overwrite,
                               Long jobId, boolean simulated,
                               String targetEmpId) {

        final CalcStatus status = simulated ? CalcStatus.SIMULATED : CalcStatus.CALCULATED;

        List<PayRule> rules = payRuleRepo.findActiveValidRules(ActiveStatus.ACTIVE, LocalDate.now());
        List<PayItemMst> items = itemRepo.findAll();
        List<PayCalcRule> calcRules = calcRuleRepo.findAll();
        List<SimpleEmp> employees = employeePort.findActiveEmployees();

        // 🔥 개별 계산인 경우: 해당 사원만 필터링
        if (targetEmpId != null && !targetEmpId.isBlank()) {
            employees = employees.stream()
                    .filter(e -> targetEmpId.equals(e.empId()))
                    .toList();
        }

        if (employees == null || employees.isEmpty())
            return 0;

        int count = 0;

        // 🔥 계산월의 말일
        LocalDate calcMonthEnd = LocalDate.parse(payYymm + "01", DateTimeFormatter.ofPattern("yyyyMMdd"))
                .withDayOfMonth(LocalDate.parse(payYymm + "01", DateTimeFormatter.ofPattern("yyyyMMdd")).lengthOfMonth());

        for (SimpleEmp emp : employees) {

            try {
                // 🔥 입사일 조건 체크: 입사일이 계산월 말일 이후이면 제외
                if (emp.hireDate() != null && emp.hireDate().isAfter(calcMonthEnd)) {
                    log.info("입사일로 제외됨 → empId={}, hireDate={}, calcMonthEnd={}",
                            emp.empId(), emp.hireDate(), calcMonthEnd);
                    continue;
                }

                // 이미 계산된 건 skip
                if (!overwrite && payslipRepo.existsByPayYymmAndEmpId(payYymm, emp.empId()))
                    continue;

                // ------- 기존 급여 계산 로직 그대로 --------
                BigDecimal baseAmt = calcBase(emp, rules, items, calcRules);
                BigDecimal alwAmt  = calcAllowances(emp, rules, items, calcRules, baseAmt);
                BigDecimal dedAmt  = calcDeductions(emp, rules, items, calcRules, baseAmt, alwAmt);

                BigDecimal totAmt = baseAmt.add(alwAmt);
                BigDecimal netAmt = totAmt.subtract(dedAmt);

                PayrollPayslip slip = payslipRepo
                        .findByPayYymmAndEmpId(payYymm, emp.empId())
                        .orElse(new PayrollPayslip());

                boolean isNew = (slip.getPayslipId() == null);

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
                slip.setCalcDt(LocalDateTime.now());
                slip.setJobId(jobId);

                if (isNew) {
                    em.persist(slip);
                    em.flush();
                } else {
                    em.merge(slip);
                    em.flush();
                }

                /* =====================================================
                 *  🔥 지급/공제 항목 저장 (EMP_PAY_ITEM) — 상세항목 저장
                 * ===================================================== */
                empPayItemRepo.deleteByPayslipPayslipId(slip.getPayslipId());

                int sort = 1;
                
             // ==================== 급여 규칙 찾기 ====================
                PayRule rule = rules.stream().findFirst().orElse(null);
                if (rule == null) {
                    log.warn("적용 가능한 PayRule 없음 → {}", emp.empId());
                    continue;
                }

                // ========= 공통 계산 =========
                BigDecimal total = baseAmt.add(alwAmt);

                BigDecimal penRate  = BigDecimal.valueOf(rule.getPenRate());
                BigDecimal hlthRate = BigDecimal.valueOf(rule.getHlthRate());
                BigDecimal empRate  = BigDecimal.valueOf(rule.getEmpRate());
                BigDecimal taxRate  = BigDecimal.valueOf(rule.getTaxRate());


                // ========= 지급항목 저장 =========

                // 지급: 기본급
                empPayItemRepo.save(EmpPayItem.builder()
                        .payslip(slip)
                        .itemType("ALW")
                        .itemCode("BASE")
                        .itemName("기본급")
                        .amount(baseAmt)
                        .sortNo(sort++)
                        .build());

                // 지급: 식대
                BigDecimal mealAmt = BigDecimal.valueOf(rule.getMealAmt() == null ? 0.0 : rule.getMealAmt());
                empPayItemRepo.save(EmpPayItem.builder()
                        .payslip(slip)
                        .itemType("ALW")
                        .itemCode("MEAL")
                        .itemName("식대")
                        .amount(mealAmt)
                        .sortNo(sort++)
                        .build());

                // 지급: 교통비
                BigDecimal transAmt = BigDecimal.valueOf(rule.getTransAmt() == null ? 0.0 : rule.getTransAmt());
                empPayItemRepo.save(EmpPayItem.builder()
                        .payslip(slip)
                        .itemType("ALW")
                        .itemCode("TRANS")
                        .itemName("교통비")
                        .amount(transAmt)
                        .sortNo(sort++)
                        .build());

                // 지급 합계
//                empPayItemRepo.save(EmpPayItem.builder()
//                        .payslip(slip)
//                        .itemType("ALW")
//                        .itemCode("ALW_SUM")
//                        .itemName("수당 합계")
//                        .amount(alwAmt)
//                        .sortNo(sort++)
//                        .build());


                // ========= 공제항목 저장 =========

                // 국민연금
                BigDecimal pension = total.multiply(penRate).setScale(0, RoundingMode.DOWN);
                empPayItemRepo.save(EmpPayItem.builder()
                        .payslip(slip)
                        .itemType("DED")
                        .itemCode("PENSION")
                        .itemName("국민연금")
                        .amount(pension)
                        .sortNo(sort++)
                        .build());

                // 건강보험
                BigDecimal health = total.multiply(hlthRate).setScale(0, RoundingMode.DOWN);
                empPayItemRepo.save(EmpPayItem.builder()
                        .payslip(slip)
                        .itemType("DED")
                        .itemCode("HEALTH")
                        .itemName("건강보험")
                        .amount(health)
                        .sortNo(sort++)
                        .build());

                // 고용보험
                BigDecimal empIns = total.multiply(empRate).setScale(0, RoundingMode.DOWN);
                empPayItemRepo.save(EmpPayItem.builder()
                        .payslip(slip)
                        .itemType("DED")
                        .itemCode("EMPLOY")
                        .itemName("고용보험")
                        .amount(empIns)
                        .sortNo(sort++)
                        .build());

                // 소득세
                BigDecimal incomeTax = total.multiply(taxRate).setScale(0, RoundingMode.DOWN);
                empPayItemRepo.save(EmpPayItem.builder()
                        .payslip(slip)
                        .itemType("DED")
                        .itemCode("TAX")
                        .itemName("소득세")
                        .amount(incomeTax)
                        .sortNo(sort++)
                        .build());

                // 지방소득세(소득세 10%)
//                BigDecimal localTax = incomeTax.divide(BigDecimal.TEN, 0, RoundingMode.DOWN);
//                empPayItemRepo.save(EmpPayItem.builder()
//                        .payslip(slip)
//                        .itemType("DED")
//                        .itemCode("LOCAL_TAX")
//                        .itemName("지방소득세")
//                        .amount(localTax)
//                        .sortNo(sort++)
//                        .build());

             // 공제 합계
//                empPayItemRepo.save(EmpPayItem.builder()
//                        .payslip(slip)
//                        .itemType("DED")
//                        .itemCode("DED_SUM")
//                        .itemName("공제 합계")
//                        .amount(dedAmt)
//                        .sortNo(sort++)
//                        .build());


                count++;

            } catch (Exception e) {
                log.error("[{}] 급여 계산 실패", emp.empId(), e);
            }
        }

        return count;
    }

    /* ========================= 상세 조회 ========================= */
    public PayslipDetailDTO getPayslipDetail(String yyyymm, String empId) {

        PayrollPayslip slip = payslipRepo.findByPayYymmAndEmpId(yyyymm, empId)
                .orElseThrow(() -> new RuntimeException("데이터 없음"));

        // EMP 이름/부서명 조회 (EmployeePort에서 가져오기)
        String empName = employeePort.getEmpName(empId);
        String deptName = employeePort.getDeptName(slip.getDeptId());

        // 지급/공제 항목 조회
        List<EmpPayItem> items = empPayItemRepo.findByPayslipPayslipIdOrderBySortNo(slip.getPayslipId());

        List<PayslipDetailDTO.Item> itemDtos = items.stream()
                .map(it -> PayslipDetailDTO.Item.builder()
                        .itemName(it.getItemName())
                        .amount(it.getAmount())
                        .type(it.getItemType())
                        .build())
                .toList();

        return PayslipDetailDTO.builder()
                .empId(empId)
                .empName(empName)
                .deptName(deptName)
                .baseAmt(slip.getBaseAmt())
                .alwAmt(slip.getAlwAmt())
                .dedAmt(slip.getDedAmt())
                .netAmt(slip.getNetAmt())
                .items(itemDtos)
                .build();
    }


    /* ========================= 계산 서브로직 ========================= */

    private BigDecimal calcBase(SimpleEmp emp, List<PayRule> rules,
                                List<PayItemMst> items, List<PayCalcRule> calcRules) {

        PayRule rule = rules.stream().findFirst().orElse(null);
        return rule == null ? BigDecimal.ZERO :
                safe(rule.getBaseAmt());
    }

    private BigDecimal calcAllowances(SimpleEmp emp, List<PayRule> rules,
                                      List<PayItemMst> items, List<PayCalcRule> calcRules,
                                      BigDecimal baseAmt) {

        PayRule rule = rules.stream().findFirst().orElse(null);
        if (rule == null) return BigDecimal.ZERO;

        BigDecimal meal = BigDecimal.valueOf(Optional.ofNullable(rule.getMealAmt()).orElse(0.0));
        BigDecimal trans = BigDecimal.valueOf(Optional.ofNullable(rule.getTransAmt()).orElse(0.0));

        return safe(meal.add(trans));
    }

    private BigDecimal calcDeductions(SimpleEmp emp, List<PayRule> rules,
                                      List<PayItemMst> items, List<PayCalcRule> calcRules,
                                      BigDecimal baseAmt, BigDecimal alwAmt) {

        PayRule rule = rules.stream().findFirst().orElse(null);
        if (rule == null) return BigDecimal.ZERO;

        BigDecimal total = baseAmt.add(alwAmt);

        BigDecimal penRate = BigDecimal.valueOf(rule.getPenRate());
        BigDecimal hlthRate = BigDecimal.valueOf(rule.getHlthRate());
        BigDecimal empRate = BigDecimal.valueOf(rule.getEmpRate());
        BigDecimal taxRate = BigDecimal.valueOf(rule.getTaxRate());

        BigDecimal totalDed = total.multiply(penRate)
                .add(total.multiply(hlthRate))
                .add(total.multiply(empRate))
                .add(total.multiply(taxRate));

        return safe(totalDed);
    }

    /* ========================= 유틸 ========================= */

    private static BigDecimal safe(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    private static String optUser(String userId) {
        return (userId == null || userId.isBlank()) ? "SYSTEM" : userId;
    }

    public interface EmployeeQueryPort {
        List<SimpleEmp> findActiveEmployees();
        String getEmpName(String empId);
        String getDeptName(String deptId);
    }

    public record SimpleEmp(String empId, String deptId, LocalDate hireDate) {}

    
    /** 특정 월 계산 상태 조회 */
    public PayCalcStatusDTO getStatus(String yyyymm) {

        long count = payslipRepo.countByPayYymm(yyyymm);
        BigDecimal total = payslipRepo.sumTotalByYymm(yyyymm);
        BigDecimal ded   = payslipRepo.sumDeductByYymm(yyyymm);
        BigDecimal net   = payslipRepo.sumNetByYymm(yyyymm);
        String calcStatus = payslipRepo.findFirstStatusByYyyymm(yyyymm)
                .orElse("READY");

        boolean calculated = (count > 0);

        return new PayCalcStatusDTO(
                yyyymm,
                calculated,
                count,
                total,
                ded,
                net,
                calcStatus
        );
    }
    
    public static String currentYymm() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }



}


