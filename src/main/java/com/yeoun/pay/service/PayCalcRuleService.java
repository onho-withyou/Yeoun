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
     */
    public PayCalcRule save(PayCalcRule form) {

        // 1) ITEM_CODE → 영속 엔티티 매핑
        normalizeItemReference(form);

        PayCalcRule entity;

        if (form.getRuleId() != null) {
            // 2) 기존 엔티티 조회 (수정모드)
            entity = payCalcRuleRepository.findById(form.getRuleId())
                    .orElseThrow(() -> new IllegalArgumentException("규칙이 존재하지 않습니다."));

            // **💥 완전 덮어쓰기 핵심**
            entity.setItem(form.getItem());
            entity.setRuleType(form.getRuleType());
            entity.setPriority(form.getPriority());
            entity.setStartDate(form.getStartDate());
            entity.setEndDate(form.getEndDate());
            entity.setStatus(form.getStatus());
            entity.setCalcFormula(form.getCalcFormula());
            entity.setRemark(form.getRemark());

            // 🔥 대상구분 설정
            entity.setTargetType(form.getTargetType());

            // 🔥 targetCode 완전 덮어쓰기 (가장 중요!)
            if (form.getTargetType() == TargetType.ALL) {
                entity.setTargetCode("");
            } else {
                entity.setTargetCode(form.getTargetCode()); // 새로운 값만 저장
            }

            // 🔥 숫자값 덮어쓰기
            entity.setValueNum(form.getValueNum());

        } else {
            // 신규 저장
            entity = form;
        }

        // 3) 날짜 등 비즈니스 검증
        validateBusiness(entity, entity.getRuleId());

        return payCalcRuleRepository.save(entity);
    }

    // =============================== 아래 기존 메서드 동일 ===============================

    @Transactional(readOnly = true)
    public PayCalcRule find(Long ruleId) { return payCalcRuleRepository.findById(ruleId).orElse(null); }

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

    public void delete(Long ruleId) { payCalcRuleRepository.deleteById(ruleId); }

    private void normalizeItemReference(PayCalcRule r) {
        if (r.getItem() == null || r.getItem().getItemCode() == null || r.getItem().getItemCode().isBlank())
            throw new IllegalArgumentException("급여항목을 선택하세요.");

        String code = r.getItem().getItemCode();
        PayItemMst item = payItemMstRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 급여항목입니다. itemCode=" + code));

        r.setItem(item);
    }

    private void validateBusiness(PayCalcRule r, Long excludeId) {
        if (r.getEndDate() != null && r.getEndDate().isBefore(r.getStartDate()))
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");

        if (r.getRuleType() != RuleType.FORMULA && r.getValueNum() == null)
            throw new IllegalArgumentException("숫자값을 입력하세요.");

        String targetCodeSafe =
                (r.getTargetType() == TargetType.ALL) ? "" : nullToEmpty(r.getTargetCode());

        boolean overlapped = hasOverlap(
                r.getItem().getItemCode(),
                r.getTargetType(),
                targetCodeSafe,
                r.getStartDate(),
                r.getEndDate(),
                excludeId
        );

        if (overlapped)
            throw new IllegalArgumentException("동일 항목/대상 조합에 기간이 겹치는 규칙이 이미 존재합니다.");
    }

    @Transactional(readOnly = true)
    public boolean hasOverlap(String itemCode,
                              TargetType targetType,
                              String targetCode,
                              LocalDate startDate,
                              LocalDate endDate,
                              Long excludeId) {

        List<PayCalcRule> group =
                payCalcRuleRepository.findForOverlapCheck(itemCode, targetType, targetCode);

        LocalDate aStart = startDate;
        LocalDate aEnd = (endDate != null) ? endDate : LocalDate.of(9999, 12, 31);

        for (PayCalcRule e : group) {

            if (excludeId != null && Objects.equals(excludeId, e.getRuleId())) continue;

            LocalDate bStart = e.getStartDate();
            LocalDate bEnd = (e.getEndDate() != null) ? e.getEndDate() : LocalDate.of(9999, 12, 31);

            if (!aEnd.isBefore(bStart) && !bEnd.isBefore(aStart)) return true;
        }
        return false;
    }

    private String nullToEmpty(String s) { return (s == null ? "" : s); }
}
