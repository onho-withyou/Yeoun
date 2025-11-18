package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
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
import com.yeoun.pay.enums.ItemGroup;
import com.yeoun.pay.repository.EmpPayItemRepository;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.repository.PayRuleRepository;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@RequiredArgsConstructor
@Log4j2
public class PayrollCalcService {
	
	@Getter
	@AllArgsConstructor
	static class AllowanceResult {
	    private BigDecimal allowance;   // ALW_AMT
	    private BigDecimal incentive;   // INC_AMT (직급수당)
	}



    private final PayrollPayslipRepository payslipRepo;
    private final PayRuleRepository payRuleRepo;
    private final PayItemMstRepository itemRepo;
    private final PayCalcRuleRepository calcRuleRepo;
    private final EmployeeQueryPort employeePort;
    private final EmpPayItemRepository empPayItemRepo;
    
    private static final JexlEngine JEXL = new JexlBuilder().create();

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
                AllowanceResult ar = calcAllowances(emp, rules, items, calcRules, baseAmt);
                BigDecimal alwAmt = ar.getAllowance();   // ALW
                BigDecimal incAmt = ar.getIncentive();
                
                BigDecimal dedAmt  = calcDeductions(emp, rules, items, calcRules, baseAmt, alwAmt);
                BigDecimal totAmt = baseAmt.add(alwAmt).add(incAmt);
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
                slip.setIncAmt(safe(incAmt));
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
                
             // 지급: 직급수당 (INCENTIVE)
                if (incAmt.compareTo(BigDecimal.ZERO) > 0) {
                    empPayItemRepo.save(EmpPayItem.builder()
                            .payslip(slip)
                            .itemType("ALW")
                            .itemCode("INCENTIVE")
                            .itemName("직급수당")
                            .amount(incAmt)
                            .sortNo(sort++)
                            .build());
                }


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

    private BigDecimal calcBase(SimpleEmp emp,
            List<PayRule> rules,
            List<PayItemMst> items,
            List<PayCalcRule> calcRules) {
			
			PayRule rule = rules.stream().findFirst().orElse(null);
			if (rule == null) return BigDecimal.ZERO;
			
			return safe(rule.getBaseAmt());
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

     //===========계산 ==============
    private AllowanceResult calcAllowances(SimpleEmp emp,
            List<PayRule> rules,
            List<PayItemMst> items,
            List<PayCalcRule> calcRules,
            BigDecimal baseAmt) {

        PayRule rule = rules.stream().findFirst().orElse(null);
        if (rule == null) 
            return new AllowanceResult(BigDecimal.ZERO, BigDecimal.ZERO);


        BigDecimal meal = BigDecimal.valueOf(Optional.ofNullable(rule.getMealAmt()).orElse(0.0));
        BigDecimal trans = BigDecimal.valueOf(Optional.ofNullable(rule.getTransAmt()).orElse(0.0));

        BigDecimal totalAllowance = meal.add(trans);   // ALW_AMT
        BigDecimal incentiveAmt   = BigDecimal.ZERO;   // INC_AMT → 직급수당

        log.info("=== [지급 계산 시작] empId={}, baseAmt={}, meal={}, trans={} ===",
                emp.empId(), baseAmt, meal, trans);

        /* =======================================================
           🔥  PayCalcRule 기반 수당 계산
        ======================================================== */
        for (PayCalcRule cr : calcRules) {

            if (cr.getItem() == null) {
                log.warn("🚨 PayCalcRule {} 의 ITEM 이 NULL 입니다. ITEM_CODE 를 확인하세요.", cr.getRuleId());
                continue;
            }

            ItemGroup group = cr.getItem().getItemGroup();
            if (group == null) {
                log.warn("🚨 PayCalcRule {} ITEM_GROUP 이 NULL 입니다. ITEM_CODE={}", cr.getRuleId(), cr.getItem().getItemCode());
                continue;
            }

            if (!List.of(ItemGroup.ALLOWANCE, ItemGroup.INCENTIVE).contains(group))
                continue;


            // 규칙-항목 매칭
            PayItemMst item = cr.getItem();
            if (item == null) continue;

            // 대상 조건
            boolean targetPass = false;
            switch (cr.getTargetType()) {
                case ALL -> targetPass = true;
                case EMP -> targetPass = emp.empId().equals(cr.getTargetCode());
                case DEPT -> targetPass = emp.deptId().equals(cr.getTargetCode());
                case GRADE -> {
                    String pos = employeePort.getEmpPosition(emp.empId());
                    log.info("사원 직급={}", pos);
                    targetPass = pos.equals(cr.getTargetCode());
                }
            }
            if (!targetPass) continue;

            // === JEXL 변수 ===
            Map<String, Object> vars = new HashMap<>();
            vars.put("BASE_AMT", baseAmt);
            vars.put("value", cr.getValueNum());
            vars.put("rate", cr.getValueNum());

            int usedAnnual = employeePort.getUsedAnnual(emp.empId());
            vars.put("remain_days", usedAnnual);

            JexlContext ctx = new MapContext(vars);

            try {
                log.info("  → 수식 실행: ruleId={}, expr={}, vars={}",
                        cr.getRuleId(), cr.getCalcFormula(), vars);

                JexlExpression expr = JEXL.createExpression(cr.getCalcFormula());
                BigDecimal result = new BigDecimal(expr.evaluate(ctx).toString());

                log.info("  → 계산 결과: empId={}, ruleId={}, 금액={}",
                        emp.empId(), cr.getRuleId(), result);

                /* ----------------------------------------------
                 🔥 직급(GRADE) 수당은 incentive 로 저장!
                 ---------------------------------------------- */
                if (cr.getTargetType().name().equals("GRADE")) {
                    incentiveAmt = incentiveAmt.add(result);
                } else {
                    totalAllowance = totalAllowance.add(result);
                }

            } catch (Exception e) {
                log.error("  → [ERROR] ruleId={} 계산 실패: {}", cr.getRuleId(), e.getMessage());
            }
        }

        // === 결과 로그 ===
        log.info("=== [지급 계산 종료] empId={}, totalAllowance(ALW)={}, incentiveAmt(INC)={} ===",
                emp.empId(), totalAllowance, incentiveAmt);

        // 👉 여기서는 ALW만 반환 (INC는 호출부에서 저장)
        return new AllowanceResult(
        	    safe(totalAllowance),
        	    safe(incentiveAmt)
        	);
    }

    /* =======================================================
	🔥  공제 금액 계산 
	======================================================= */

    
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
        String getEmpPosition(String empId);  // 직급코드 반환
        int getUsedAnnual(String empId);      // 올해 사용한 연차일수
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


