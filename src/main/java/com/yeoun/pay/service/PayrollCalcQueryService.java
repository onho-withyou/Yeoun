// src/main/java/com/yeoun/pay/service/PayrollCalcQueryService.java
package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.pay.entity.PayrollPayslip;
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollCalcQueryService {

    private final PayrollPayslipRepository payslipRepo;

    /** 화면 표시에 사용할 리스트: 확정(CALCULATED)이 있으면 그걸, 없으면 시뮬레이션(SIMULATED)을 보여줌 */    
    
    public List<PayrollPayslip> findForView(String yyyymm) {
        List<PayrollPayslip> confirmed =
            payslipRepo.findByPayYymmAndCalcStatusOrderByEmpIdAsc(yyyymm, CalcStatus.CALCULATED);
        if (!confirmed.isEmpty()) return confirmed;
        return payslipRepo.findByPayYymmAndCalcStatusOrderByEmpIdAsc(yyyymm, CalcStatus.SIMULATED);
    }


    /** 합계 계산 */
    public long[] totals(List<PayrollPayslip> list) {
        long totPay = 0L, totDed = 0L, net = 0L;
        for (PayrollPayslip p : list) {
            totPay += n(p.getTotAmt()); // 총지급
            totDed += n(p.getDedAmt()); // 공제
            net    += n(p.getNetAmt()); // 실수령
        }
        return new long[]{totPay, totDed, net};
    }

    /** BigDecimal → long 변환 (null 안전 처리) */
    private long n(BigDecimal v) {
        return (v == null) ? 0L : v.longValue();
    }
}