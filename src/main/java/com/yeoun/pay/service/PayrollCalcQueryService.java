
package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.pay.dto.PayslipViewDTO;
import com.yeoun.pay.enums.CalcStatus;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollCalcQueryService {

    private final PayrollPayslipRepository payslipRepo;

    /** 
     * ✅ 화면 표시용 급여명세서 조회 (상태 구분 없이 전부 조회)
     */
 
    public List<PayslipViewDTO> findForView(String yyyymm) {
        return payslipRepo.findPayslipsWithEmpAndDept(yyyymm, null);
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