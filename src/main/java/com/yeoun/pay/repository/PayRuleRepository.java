package com.yeoun.pay.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.enums.ActiveStatus;

public interface PayRuleRepository extends JpaRepository<PayRule, Long> {

    Page<PayRule> findByStatus(ActiveStatus status, Pageable pageable);

    Page<PayRule> findByStartDateBetween(LocalDate from, LocalDate to, Pageable pageable);
    Page<PayRule> findByStartDateGreaterThanEqual(LocalDate from, Pageable pageable);
    Page<PayRule> findByStartDateLessThanEqual(LocalDate to, Pageable pageable);

    Page<PayRule> findByStatusAndStartDateBetween(ActiveStatus status, LocalDate from, LocalDate to, Pageable pageable);
    Page<PayRule> findByStatusAndStartDateGreaterThanEqual(ActiveStatus status, LocalDate from, Pageable pageable);
    Page<PayRule> findByStatusAndStartDateLessThanEqual(ActiveStatus status, LocalDate date, Pageable pageable);
}


