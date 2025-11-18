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
    private final EmpPayItemRepository
    itemRepo;

    public EmpPayslipResponseDTO getDetail(Long payslipId) {

        // 1) 기본 정보 조회 (interface projection)
        EmpPayslipDetailDTO header = repo.findDetail(payslipId);
        if (header == null) return null;

        // 2) 항목 상세 조회
        var items = itemRepo.findByPayslipPayslipIdOrderBySortNo(payslipId)
                .stream()
                .map(i -> EmpPayslipResponseDTO.EmpPayslipItem.builder()
                        .itemName(i.getItemName())
                        .amount(i.getAmount())
                        .type(i.getItemType())
                        .build()
                ).toList();

        // 3) header + items 조합하여 최종 DTO 생성
        return EmpPayslipResponseDTO.builder()
                .header(header)
                .items(items)
                .build();
    }
}
