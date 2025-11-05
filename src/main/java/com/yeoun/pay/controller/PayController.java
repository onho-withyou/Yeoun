package com.yeoun.pay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pay")
public class PayController {
    
    // 급여 기준정보 페이지
    @GetMapping("/pay_rule")
    public String payRule() {
        return "pay/pay_rule"; // templates/pay/pay_rule.html
    }
    
    // 급여 계산
    @GetMapping("/payroll_payslip")
    public String payrollPayslip() {
        return "pay/payroll_payslip"; // templates/pay/payroll_payslip.html
    }
    
 // 급여 명세서 페이지
    @GetMapping("/emp_pay")
    public String empPay() {
        return "pay/emp_pay"; // templates/pay/payroll_payslip.html
    }
}

