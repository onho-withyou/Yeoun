package com.yeoun.pay.controller;

import com.yeoun.pay.service.PayrollHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pay/history")
@RequiredArgsConstructor
@Slf4j
public class PayrollHistoryController {

    private final PayrollHistoryService service;

    @GetMapping
    public String historyPage(

            @RequestParam(name = "mode", defaultValue = "emp") String mode,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "deptName", required = false) String deptName,

            @RequestParam(name = "yearEmp", required = false) String yearEmp,
            @RequestParam(name = "monthEmp", required = false) String monthEmp,

            @RequestParam(name = "yearDept", required = false) String yearDept,
            @RequestParam(name = "monthDept", required = false) String monthDept,

            @RequestParam(name = "yearMonth", required = false) String yearMonth,
            @RequestParam(name = "monthMonth", required = false) String monthMonth,

            Model model
    ) {

        String year = null, month = null;

        if (mode.equals("emp")) {
            year = yearEmp;
            month = monthEmp;
        } else if (mode.equals("dept")) {
            year = yearDept;
            month = monthDept;
        } else if (mode.equals("month")) {
            year = yearMonth;
            month = monthMonth;
        }

        model.addAttribute("slips",
                service.search(mode, keyword, deptName, year, month));

        model.addAttribute("mode", mode);

        return "pay/pay_history";
    }
}
