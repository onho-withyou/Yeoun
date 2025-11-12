
package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.pay.dto.PayslipViewDTO;
import com.yeoun.pay.entity.PayrollPayslip;
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollCalcQueryService {

    private final PayrollPayslipRepository payslipRepo;

    /** 
     * 화면 표시용 급여명세서 조회
     * 확정(CALCULATED)이 있으면 그걸, 없으면 시뮬레이션(SIMULATED)을 보여줌
     */
    public List<PayslipViewDTO> findForView(String yyyymm) {

        // 1️⃣ CALCULATED 먼저 조회
        List<PayslipViewDTO> confirmed =
            payslipRepo.findPayslipsWithEmpAndDept(yyyymm, CalcStatus.CALCULATED);

        // 2️⃣ 없으면 SIMULATED로 대체
        if (!confirmed.isEmpty()) return confirmed;

        return payslipRepo.findPayslipsWithEmpAndDept(yyyymm, CalcStatus.SIMULATED);
    }



    /** 합계 계산 */
    public long[] totals(List<PayslipViewDTO> list) {
        long totPay = 0L, totDed = 0L, net = 0L;
        for (PayslipViewDTO p : list) {
            totPay += n(p.getTotAmt());
            totDed += n(p.getDedAmt());
            net    += n(p.getNetAmt());
        }
        return new long[]{totPay, totDed, net};
    }


    /** BigDecimal → long 변환 (null 안전 처리) */
    private long n(BigDecimal v) {
        return (v == null) ? 0L : v.longValue();
    }
    
    
}