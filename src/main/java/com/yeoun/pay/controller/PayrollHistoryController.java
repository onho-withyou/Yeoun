package com.yeoun.pay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pay/history")
public class PayrollHistoryController {

    @GetMapping
    public String page() {
        return "pay/pay_history"; 
    }
}
