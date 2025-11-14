
package com.yeoun.pay.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayCalcStatusService {

    private final PayrollPayslipRepository repo;

    public PayCalcStatusDTO getStatus(String yyyymm) {
        boolean done = repo.existsByPayYymm(yyyymm);
        long cnt     = repo.countByPayYymm(yyyymm);
        BigDecimal tot = repo.sumTotalByYymm(yyyymm);
        BigDecimal ded = repo.sumDeductByYymm(yyyymm);
        BigDecimal net = repo.sumNetByYymm(yyyymm);
        String calcStatus = repo.findFirstStatusByYyyymm(yyyymm)
                .orElse("READY");
        
        return new PayCalcStatusDTO(yyyymm, done, cnt, tot, ded, net,calcStatus);
    }
}
