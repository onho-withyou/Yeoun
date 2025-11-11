package com.yeoun.pay.service;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.enums.TargetType;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
     * [C/U] 저장/수정
     * - item은 itemCode만 채워서 넘어와도 DB에서 영속 엔티티로 교체
     * - priority가 null이면 @ColumnDefault(100)로 DB에서 기본값 적용(@DynamicInsert)
     * - 기간/필수값/중복 검증
     */
    public PayCalcRule save(PayCalcRule rule) {
        normalizeItemReference(rule);
        validate(rule, rule.getRuleId()); // 수정 시 자기 자신 제외
        return payCalcRuleRepository.save(rule);
    }

    /** [R] 단건 조회 (없으면 null) */
    @Transactional(readOnly = true)
    public PayCalcRule find(Long ruleId) {
        return payCalcRuleRepository.findById(ruleId).orElse(null);
    }

    /** [R] 항목별 전체 목록 (우선순위 asc) */
    @Transactional(readOnly = true)
    public List<PayCalcRule> findByItem(String itemCode) {
        return payCalcRuleRepository.findByItem_ItemCodeOrderByPriorityAsc(itemCode);
    }

    /** [R] 항목 + 대상 조건 목록 (우선순위 asc) */
    @Transactional(readOnly = true)
    public List<PayCalcRule> findByItemAndTarget(String itemCode, TargetType targetType, String targetCode) {
        return payCalcRuleRepository.findByItem_ItemCodeAndTargetTypeAndTargetCodeOrderByPriorityAsc(
                itemCode, targetType, targetCode);
    }

    /** [D] 삭제 */
    public void delete(Long ruleId) {
        payCalcRuleRepository.deleteById(ruleId);
        
        
    }

    /* ==================== 내부 검증/도우미 ==================== */

    /** 컨트롤러에서 itemCode만 채워 보냈을 수도 있으니 영속 엔티티로 교체 */
    private void normalizeItemReference(PayCalcRule r) {
        if (r.getItem() == null || r.getItem().getItemCode() == null || r.getItem().getItemCode().isBlank()) {
            throw new IllegalArgumentException("item(ITEM_CODE)은 필수입니다.");
        }
        String code = r.getItem().getItemCode();
        PayItemMst item = payItemMstRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 급여항목입니다. itemCode=" + code));
        r.setItem(item); // 영속 엔티티로 교체
    }

    private void validate(PayCalcRule r, Long excludeId) {
        // 필수값
        if (r.getRuleType() == null) throw new IllegalArgumentException("ruleType은 필수입니다.");
        if (r.getStatus() == null)   throw new IllegalArgumentException("status는 필수입니다.");
        if (r.getStartDate() == null) throw new IllegalArgumentException("startDate는 필수입니다.");

        // 날짜 일관성
        if (r.getEndDate() != null && r.getEndDate().isBefore(r.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        // 수치값 유효성 (음수 금지)
        if (ltZero(r.getValueNum())) {
            throw new IllegalArgumentException("valueNum은 0 이상이어야 합니다.");
        }

        // 기간 중복 체크: 같은 항목 + 같은 대상 조합 내에서 겹치면 안 됨
        String itemCode = r.getItem().getItemCode();
        if (hasOverlap(
                itemCode,
                r.getTargetType(),
                nullToEmpty(r.getTargetCode()),
                r.getStartDate(),
                r.getEndDate(),
                excludeId
        )) {
            throw new IllegalArgumentException("동일 항목/대상에 기간이 겹치는 규칙이 이미 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public boolean hasOverlap(String itemCode,
                              TargetType targetType,
                              String targetCode,
                              LocalDate startDate,
                              LocalDate endDate,
                              Long excludeId) {

        // 대상 기준: targetType/targetCode가 모두 null/빈값이면 '전체 대상'으로 간주
        List<PayCalcRule> group = payCalcRuleRepository
                .findForOverlapCheck(itemCode, targetType, nullToEmpty(targetCode));

        LocalDate aStart = startDate;
        LocalDate aEnd = (endDate != null) ? endDate : LocalDate.of(9999, 12, 31);

        for (PayCalcRule e : group) {
            if (excludeId != null && Objects.equals(excludeId, e.getRuleId())) continue;

            LocalDate bStart = e.getStartDate();
            LocalDate bEnd = (e.getEndDate() != null) ? e.getEndDate() : LocalDate.of(9999, 12, 31);

            // [aStart, aEnd] 와 [bStart, bEnd] 겹치면 true
            if (!aEnd.isBefore(bStart) && !bEnd.isBefore(aStart)) {
                return true;
            }
        }
        return false;
    }

    private boolean ltZero(BigDecimal v) {
        return v != null && v.signum() < 0;
    }

    private String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }
    

    @Transactional(readOnly = true)
    public List<PayCalcRule> findAllOrderByPriority() {
        return payCalcRuleRepository.findAllByOrderByPriorityAsc();
    }

	



}
