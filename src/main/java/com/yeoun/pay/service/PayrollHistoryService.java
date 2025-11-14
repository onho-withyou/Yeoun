package com.yeoun.pay.service;

import com.yeoun.pay.dto.PayrollHistoryProjection;
import com.yeoun.pay.repository.PayrollHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollHistoryService {

    private final PayrollHistoryRepository repo;

    public List<PayrollHistoryProjection> search(
            String mode,
            String keyword,
            String deptName,
            String year,
            String month
    ) {

        String yymm = null;

        if (year != null && month != null && !year.isEmpty() && !month.isEmpty()) {
            yymm = year + month;
        }

        log.info("=== FINAL SEARCH PARAMS ===");
        log.info("mode = {}", mode);
        log.info("keyword = {}", keyword);
        log.info("deptName = {}", deptName);
        log.info("yymm = {}", yymm);

        return switch (mode) {
            case "emp" -> repo.searchByEmp(keyword, yymm);
            case "dept" -> repo.searchByDept(deptName, yymm);
            case "month" -> repo.searchByMonth(yymm);
            default -> List.of();
        };
    }
}
