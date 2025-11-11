// src/main/java/com/yeoun/pay/controller/PayCalcPageController.java
package com.yeoun.pay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.service.PayCalcStatusService;
import com.yeoun.pay.service.PayrollCalcQueryService;
import com.yeoun.pay.service.PayrollCalcService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/pay/calc")
@RequiredArgsConstructor
public class PayCalcPageController {

    private final PayCalcStatusService statusSvc;
    private final PayrollCalcService payrollCalcService;
    private final PayrollCalcQueryService querySvc; // ← 추가

    @GetMapping
    public String page(@RequestParam(name = "yyyymm", required = false) String yyyymm, Model model) {
        String mm = (yyyymm == null || yyyymm.isBlank())
                ? PayrollCalcService.currentYymm() : yyyymm;

        var slips = querySvc.findForView(mm);         // ← 리스트 조회
        var t = querySvc.totals(slips);               // ← 합계

        model.addAttribute("yyyymm", mm);
        model.addAttribute("status", statusSvc.getStatus(mm));
        model.addAttribute("slips", slips);           // ← 화면 테이블에 바인딩할 리스트
        model.addAttribute("sumPay", t[0]);
        model.addAttribute("sumDed", t[1]);
        model.addAttribute("sumNet", t[2]);

        return "pay/pay_calc_run";
    }

    @PostMapping("/simulate")
    public String simulate(@RequestParam String yyyymm,
                           @RequestParam(defaultValue = "true") boolean overwrite,
                           RedirectAttributes ra) {
        int cnt = payrollCalcService.simulateMonthly(yyyymm, overwrite);
        ra.addFlashAttribute("msg", "가계산 완료: " + yyyymm + " (" + cnt + "건)");
        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam String yyyymm,
                          @RequestParam(defaultValue = "true") boolean overwrite,
                          RedirectAttributes ra) {
        int cnt = payrollCalcService.confirmMonthly(yyyymm, overwrite);
        ra.addFlashAttribute("msg", "확정 완료: " + yyyymm + " (" + cnt + "건)");
        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }
}