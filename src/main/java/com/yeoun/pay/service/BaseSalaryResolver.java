package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.enums.TargetType;
import com.yeoun.pay.repository.PayCalcRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BaseSalaryResolver {

    private static final String BASE_ITEM_CODE = "BASE_AMT"; // 마스터의 기본급 항목코드
    private final PayCalcRuleRepository ruleRepo;

    /**
     * 규칙에서 기본급을 찾는다. 우선순위: EMP > POSITION(GRADE) > DEPT > COMMON
     * 기간(start/end)과 상태(ACTIVE), 우선순위(priority asc) 고려
     */
    public BigDecimal resolve(String empId, String deptId, String posCode, LocalDate asOf) {
        List<PayCalcRule> rules = ruleRepo.findActiveByItemAndDate(BASE_ITEM_CODE, asOf);

        // 우선순위(priority asc) → 우리가 원하는 타깃 우선순위로 재정렬
        Comparator<PayCalcRule> cmp = Comparator
                .comparingInt(r -> r.getPriority() == null ? 100 : r.getPriority());

        // 1) EMP
        BigDecimal v = pick(rules, TargetType.EMP, empId, cmp);
        if (v != null) return v;

        // 2) POSITION (네 enum에서 GRADE/position 대응)
        v = pick(rules, TargetType.GRADE, posCode, cmp);
        if (v != null) return v;

        // 3) DEPT
        v = pick(rules, TargetType.DEPT, deptId, cmp);
        if (v != null) return v;

        // 4) COMMON (targetType null or targetCode null)
        v = pick(rules, null, null, cmp);
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal pick(List<PayCalcRule> rules, TargetType tt, String code, Comparator<PayCalcRule> cmp) {
        return rules.stream()
                .filter(r -> (tt == null && r.getTargetType() == null)
                          || (r.getTargetType() == tt && eq(r.getTargetCode(), code)))
                .sorted(cmp)
                .map(this::asFixedAmount)
                .filter(x -> x != null)
                .findFirst()
                .orElse(null);
    }

    private boolean eq(String a, String b) {
        return (a == null && b == null) || (a != null && a.equalsIgnoreCase(b));
    }

    /** 기본급은 FIXED 금액 사용(혹시 RATE/FORMULA가 등록되었으면 valueNum만 사용) */
    private BigDecimal asFixedAmount(PayCalcRule r) {
        return r.getAmount() != null ? r.getAmount() : r.getValueNum();
    }
}
