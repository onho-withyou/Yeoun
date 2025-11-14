package com.yeoun.pay.service;

import com.yeoun.pay.dto.PayrollHistoryRow;
import com.yeoun.pay.repository.PayrollHistoryRepository;
import com.yeoun.pay.dto.PayrollHistoryProjection;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollHistoryService {

    private final PayrollHistoryRepository repo;

    public List<PayrollHistoryRow> search(
            String mode,
            String keyword,
            String deptName,
            String year,
            String month
    ) {

        // 🔹 yymm 조립
        String yymm = null;
        if (year != null && !year.isBlank()) {
            yymm = year + (month == null ? "" : month);
        }

        log.info("=== FINAL SEARCH PARAMS ===");
        log.info("mode = {}", mode);
        log.info("keyword = {}", keyword);
        log.info("deptName = {}", deptName);
        log.info("yymm = {}", yymm);

        List<PayrollHistoryProjection> list;

        switch (mode) {
            case "emp" -> list = repo.searchByEmp(keyword, yymm);
            case "dept" -> list = repo.searchByDept(deptName, yymm);
            case "month" -> list = repo.searchByMonth(yymm);
            default -> list = List.of();
        }

        return list.stream()
                .map(p -> PayrollHistoryRow.builder()
                        .empId(p.getEmpId())
                        .empName(p.getEmpName())
                        .deptName(p.getDeptName())
                        .payYymm(p.getPayYymm())

                        .baseAmt(p.getBaseAmt())
                        .alwAmt(p.getAlwAmt())
                        .dedAmt(p.getDedAmt())
                        .totAmt(p.getTotAmt())
                        .netAmt(p.getNetAmt())

                        .calcStatus(p.getCalcStatus())
                        .build()
                ).collect(Collectors.toList());
    }
}
