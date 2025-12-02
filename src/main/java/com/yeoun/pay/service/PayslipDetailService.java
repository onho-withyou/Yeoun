package com.yeoun.pay.service;

import org.springframework.stereotype.Service;

import com.yeoun.pay.dto.EmpPayslipDetailDTO;
import com.yeoun.pay.dto.EmpPayslipResponseDTO;
import com.yeoun.pay.repository.EmpPayItemRepository;
import com.yeoun.pay.repository.PayrollHistoryRepository;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayslipDetailService {

    private final PayrollHistoryRepository repo;
    private final EmpPayItemRepository itemRepo;

    public EmpPayslipResponseDTO getDetail(Long payslipId) {

        // 1) 기본 정보 조회
        EmpPayslipDetailDTO header = repo.findDetail(payslipId);
        if (header == null) return null;

        // 🔥 1-1) payYymm 분해 (202511 → 2025, 11)
        String yymm = header.getPayYymm();   // ← DTO에 반드시 있어야 함!!
        String year = yymm.substring(0, 4);
        String month = yymm.substring(4, 6);

        String payFormatted = year + "년 " + month + "월";

        // 2) 항목 상세 조회
        var items = itemRepo.findByPayslipPayslipIdOrderBySortNo(payslipId)
                .stream()
                .map(i -> EmpPayslipResponseDTO.EmpPayslipItem.builder()
                        .itemName(i.getItemName())
                        .amount(i.getAmount())
                        .type(i.getItemType())   // ALW / DED
                        .build()
                ).toList();

        // 3) 최종 DTO 생성
        return EmpPayslipResponseDTO.builder()
                .header(header)
                .items(items)               
                .payYymmFormatted(payFormatted)
                .build();
    }
}
