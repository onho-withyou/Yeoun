package com.yeoun.pay.repository;

import com.yeoun.pay.entity.PayRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayRunRepository extends JpaRepository<PayRun, Long> {
}
