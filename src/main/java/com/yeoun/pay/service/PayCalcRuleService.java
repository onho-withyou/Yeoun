package com.yeoun.pay.service;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.enums.RuleType;
import com.yeoun.pay.enums.TargetType;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PayCalcRuleService {

    private final PayCalcRuleRepository payCalcRuleRepository;
    private final PayItemMstRepository payItemMstRepository;


    /**
     * 저장/수정
     * - 컨트롤러에서 기본 입력 검증 완료
     * - 서비스는 비즈니스 검증만 처리 (날짜/기간/항목확인)
     */
    public PayCalcRule save(PayCalcRule rule) {

        // ITEM_CODE → 영속 엔티티 매핑
        normalizeItemReference(rule);

        // 날짜 및 비즈니스 규칙 검증
        validateBusiness(rule, rule.getRuleId());

        return payCalcRuleRepository.save(rule);
    }


    /** 단건 조회 */
    @Transactional(readOnly = true)
    public PayCalcRule find(Long ruleId) {
        return payCalcRuleRepository.findById(ruleId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PayCalcRule> findByItem(String itemCode) {
        return payCalcRuleRepository.findByItem_ItemCodeOrderByPriorityAsc(itemCode);
    }

    @Transactional(readOnly = true)
    public List<PayCalcRule> findByItemAndTarget(String itemCode, TargetType targetType, String targetCode) {
        return payCalcRuleRepository.findByItem_ItemCodeAndTargetTypeAndTargetCodeOrderByPriorityAsc(
                itemCode, targetType, targetCode);
    }

    @Transactional(readOnly = true)
    public List<PayCalcRule> findAllOrderByPriority() {
        return payCalcRuleRepository.findAllByOrderByPriorityAsc();
    }

    public void delete(Long ruleId) {
        payCalcRuleRepository.deleteById(ruleId);
    }



    /* ============================================================
          내부 로직 — 비즈니스 검증만 담당
       ============================================================ */

    /** 급여항목 존재 여부 확인 후 영속 엔티티로 교체 */
    private void normalizeItemReference(PayCalcRule r) {

        if (r.getItem() == null ||
                r.getItem().getItemCode() == null ||
                r.getItem().getItemCode().isBlank()) {
            throw new IllegalArgumentException("급여항목을 선택하세요.");
        }

        String code = r.getItem().getItemCode();

        PayItemMst item = payItemMstRepository.findById(code)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 급여항목입니다. itemCode=" + code)
                );

        r.setItem(item);
    }



    /** 비즈니스 규칙 검사 */
    private void validateBusiness(PayCalcRule r, Long excludeId) {

        // 날짜 검증
        if (r.getEndDate() != null && r.getEndDate().isBefore(r.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        // FORMULA는 valueNum 없어도 됨
        if (r.getRuleType() == RuleType.FORMULA) {
            // OK
        } else {
            // AMT / RATE 는 숫자 필수
            if (r.getValueNum() == null) {
                throw new IllegalArgumentException("숫자값을 입력하세요.");
            }
        }

        // 대상 ALL일 경우 code 무시
        String targetCodeSafe =
                (r.getTargetType() == TargetType.ALL) ? "" : nullToEmpty(r.getTargetCode());

        // 기간 겹침 검사
        boolean overlapped = hasOverlap(
                r.getItem().getItemCode(),
                r.getTargetType(),
                targetCodeSafe,
                r.getStartDate(),
                r.getEndDate(),
                excludeId
        );

        if (overlapped) {
            throw new IllegalArgumentException("동일 항목/대상 조합에 기간이 겹치는 규칙이 이미 존재합니다.");
        }
    }



    /** 기간 겹침 체크 */
    @Transactional(readOnly = true)
    public boolean hasOverlap(String itemCode,
                              TargetType targetType,
                              String targetCode,
                              LocalDate startDate,
                              LocalDate endDate,
                              Long excludeId) {

        List<PayCalcRule> group = payCalcRuleRepository
                .findForOverlapCheck(itemCode, targetType, targetCode);

        LocalDate aStart = startDate;
        LocalDate aEnd = (endDate != null) ? endDate : LocalDate.of(9999, 12, 31);

        for (PayCalcRule e : group) {

            if (excludeId != null && Objects.equals(excludeId, e.getRuleId())) continue;

            LocalDate bStart = e.getStartDate();
            LocalDate bEnd = (e.getEndDate() != null) ? e.getEndDate() : LocalDate.of(9999, 12, 31);

            // [aStart, aEnd] 와 [bStart, bEnd] 겹침 여부
            if (!aEnd.isBefore(bStart) && !bEnd.isBefore(aStart)) {
                return true;
            }
        }
        return false;
    }



    private String nullToEmpty(String s) {
        return (s == null ? "" : s);
    }
}
