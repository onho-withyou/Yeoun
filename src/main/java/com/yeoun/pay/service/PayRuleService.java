package com.yeoun.pay.service;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.enums.ActiveStatus;
import com.yeoun.pay.repository.PayRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용
public class PayRuleService {

    private final PayRuleRepository payRuleRepository;

    // 페이징 + 필터
    public Page<PayRule> findPage(ActiveStatus status,
                                  LocalDate startFrom,
                                  LocalDate startTo,
                                  Pageable pageable) {

        if (startFrom != null && startTo != null && startFrom.isAfter(startTo)) {
            throw new IllegalArgumentException("startFrom은 startTo보다 이후일 수 없습니다.");
        }

        if (status != null && startFrom != null && startTo != null) {
            return payRuleRepository.findByStatusAndStartDateBetween(status, startFrom, startTo, pageable);
        } else if (status != null && (startFrom != null || startTo != null)) {
            // 단일 경계 처리
            if (startFrom != null) {
                return payRuleRepository.findByStatusAndStartDateGreaterThanEqual(status, startFrom, pageable);
            } else {
                return payRuleRepository.findByStatusAndStartDateLessThanEqual(status, startTo, pageable);
            }
        } else if (status != null) {
            return payRuleRepository.findByStatus(status, pageable);
        } else if (startFrom != null && startTo != null) {
            return payRuleRepository.findByStartDateBetween(startFrom, startTo, pageable);
        } else if (startFrom != null) {
            return payRuleRepository.findByStartDateGreaterThanEqual(startFrom, pageable);
        } else if (startTo != null) {
            return payRuleRepository.findByStartDateLessThanEqual(startTo, pageable);
        }

        return payRuleRepository.findAll(pageable);
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
        return payRuleRepository.save(entity);
    }

    @Transactional
    public PayRule update(Long id, PayRule entity) {
        PayRule target = payRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PAY_RULE not found: " + id));

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

        return payRuleRepository.save(target); // 명시 저장(감사/이벤트 안전)
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
}
