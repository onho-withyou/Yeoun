package com.yeoun.pay.controller;

import com.yeoun.pay.dto.PayrollHistoryProjection;
import com.yeoun.pay.service.PayrollHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pay/history")
@RequiredArgsConstructor
public class PayrollHistoryRestController {

    private final PayrollHistoryService service;

    @GetMapping
    public List<PayrollHistoryProjection> search(
    		@RequestParam(name = "mode") String mode,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "deptName", required = false) String deptName,
            @RequestParam(name = "year", required = false) String year,
            @RequestParam(name = "month", required = false) String month
    ) {
        return service.search(mode, keyword, deptName, year, month);
    }
}
