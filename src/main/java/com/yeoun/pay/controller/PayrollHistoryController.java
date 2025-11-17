package com.yeoun.pay.controller;

import com.yeoun.pay.dto.PayrollHistoryProjection;
import com.yeoun.pay.service.PayrollHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pay/history")
@RequiredArgsConstructor
@Slf4j
public class PayrollHistoryController {

    private final PayrollHistoryService service;

    /** 화면 이동 */
    @GetMapping
    public String page() {
        return "pay/pay_history";
    }

    /** 검색 API (JSON 반환) */
    @GetMapping("/search")
    @ResponseBody
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
