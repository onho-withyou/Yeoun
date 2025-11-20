package com.yeoun.pay.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayCalcStatusService {

    private final PayrollPayslipRepository repo;

    public PayCalcStatusDTO getStatus(String yyyymm) {

        long totalCount     = repo.countByPayYymm(yyyymm);
        long confirmedCount  = repo.countByPayYymmAndCalcStatus(yyyymm, CalcStatus.CONFIRMED);
        long simulatedCount  = repo.countByPayYymmAndCalcStatus(yyyymm, CalcStatus.SIMULATED);
        long calculatedCount = repo.countByPayYymmAndCalcStatus(yyyymm, CalcStatus.CALCULATED);


        BigDecimal totAmt = repo.sumTotalByYymm(yyyymm);
        BigDecimal dedAmt   = repo.sumDeductByYymm(yyyymm);
        BigDecimal netAmt   = repo.sumNetByYymm(yyyymm);

        // 첫 번째 상태값(READY / SIMULATED / CALCULATED / CONFIRMED)
        String calcStatus = repo.findFirstStatusByYyyymm(yyyymm)
                                .orElse("READY");

        return PayCalcStatusDTO.builder()
                .payYymm(yyyymm)
                .totalCount(totalCount)
                .simulatedCount(simulatedCount)
                .calculatedCount(calculatedCount)
                .confirmedCount(confirmedCount)
                .totAmt(totAmt)
                .dedAmt(dedAmt)
                .netAmt(netAmt)
                .calcStatus(calcStatus)
                .build();

    }
}
