package com.yeoun.pay.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.pay.dto.PayslipDetailDTO;
import com.yeoun.pay.dto.PayslipViewDTO;
import com.yeoun.pay.entity.EmpPayItem;
import com.yeoun.pay.entity.PayrollPayslip;
import com.yeoun.pay.repository.EmpPayItemRepository;
import com.yeoun.pay.repository.PayrollPayslipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollCalcQueryService {

    private final PayrollPayslipRepository payslipRepo;
    private final EmpPayItemRepository empPayItemRepo;

    /** 화면 표시용 급여명세서 조회 (상태 구분 없이 전부 조회) */
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

    /** 급여내역 상세보기 */
    public PayslipDetailDTO getPayslipDetail(String yyyymm, String empId) {

        // 1) 해당 월/사원의 급여 결과 조회
        PayrollPayslip slip = payslipRepo
                .findByPayYymmAndEmpId(yyyymm, empId)
                .orElseThrow(() -> new RuntimeException("데이터 없음"));

        // 2) 급여 항목 상세 목록 조회
        List<EmpPayItem> items =
                empPayItemRepo.findByPayslipPayslipIdOrderBySortNo(slip.getPayslipId());

        // 3) DTO 변환
        List<PayslipDetailDTO.Item> itemDtos = items.stream()
                .map(r -> PayslipDetailDTO.Item.builder()
                        .itemName(r.getItemName())
                        .amount(r.getAmount())
                        .type(r.getItemType())  // <-- 여기 수정 (name() 제거)
                        .build()
                )
                .toList();


        // 4) 최종 DTO 반환
        return PayslipDetailDTO.builder()
                .empId(slip.getEmpId())
                .empName(slip.getEmpName())
                .deptName(slip.getDeptId())
                .baseAmt(slip.getBaseAmt())
                .alwAmt(slip.getAlwAmt())
                .dedAmt(slip.getDedAmt())
                .netAmt(slip.getNetAmt())
                .items(itemDtos)
                .build();
    }
}
