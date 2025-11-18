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

        String yymm = (year != null && month != null && !year.isEmpty() && !month.isEmpty())
                ? year + month : null;

        return repo.searchAll(keyword, deptName, yymm);
    }

}
