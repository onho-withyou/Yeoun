package com.yeoun.pay.service;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.enums.ActiveStatus;
import com.yeoun.pay.repository.PayRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayRuleService {

    private static final LocalDate MAX = LocalDate.of(9999, 12, 31);

    private final PayRuleRepository payRuleRepository;

    // 목록 조회(옵션 필터)
    public List<PayRule> list(ActiveStatus status, LocalDate startFrom, LocalDate startTo) {
        if (startFrom != null && startTo != null && startFrom.isAfter(startTo)) {
            throw new IllegalArgumentException("startFrom은 startTo보다 이후일 수 없습니다.");
        }
        return payRuleRepository.search(status, startFrom, startTo);
    }

    public List<PayRule> findAll() {
        return payRuleRepository.findAll();
    }

    public PayRule findById(Long id) {
        return payRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PAY_RULE not found: " + id));
    }

    // ===== 쓰기 트랜잭션 =====
    @Transactional
    public PayRule save(PayRule entity) {
        validateNoOverlap(entity.getStartDate(), entity.getEndDate(), null);
        return payRuleRepository.save(entity);
    }

    @Transactional
    public PayRule update(Long id, PayRule entity) {
        PayRule target = payRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PAY_RULE not found: " + id));

        validateNoOverlap(entity.getStartDate(), entity.getEndDate(), id);

        // 필요한 필드만 갱신
        target.setStartDate(entity.getStartDate());
        target.setEndDate(entity.getEndDate());
        target.setBaseAmt(entity.getBaseAmt());
        target.setMealAmt(entity.getMealAmt());
        target.setTransAmt(entity.getTransAmt());
        target.setPenRate(entity.getPenRate());
        target.setHlthRate(entity.getHlthRate());
        target.setEmpRate(entity.getEmpRate());
        target.setTaxRate(entity.getTaxRate());
        target.setPayDay(entity.getPayDay());
        target.setStatus(entity.getStatus());
        target.setRemark(entity.getRemark());

        return payRuleRepository.save(target);
    }

    @Transactional
    public PayRule changeStatus(Long id, ActiveStatus value) {
        PayRule target = payRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PAY_RULE not found: " + id));
        target.setStatus(value);
        return payRuleRepository.save(target);
    }

    @Transactional
    public void delete(Long id) {
        if (!payRuleRepository.existsById(id)) return; // idempotent
        payRuleRepository.deleteById(id);
    }

    // ===== 겹침 검사 공통 =====
    private void validateNoOverlap(LocalDate start, LocalDate end, Long excludeId) {
        if (start == null) throw new IllegalArgumentException("기준 시작일은 필수입니다.");
        if (end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("기준 시작일은 종료일 이후일 수 없습니다.");
        }
        boolean overlapped = payRuleRepository.existsOverlapping(start, end, MAX, excludeId);
        if (overlapped) {
            throw new IllegalStateException("해당 기간에 이미 등록된 급여 기준이 있어 저장할 수 없습니다.");
        }
    }
}
