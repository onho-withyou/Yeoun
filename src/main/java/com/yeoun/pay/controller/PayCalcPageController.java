// src/main/java/com/yeoun/pay/controller/PayCalcPageController.java
package com.yeoun.pay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.service.PayCalcStatusService;
import com.yeoun.pay.service.PayrollCalcService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/pay/calc")
@RequiredArgsConstructor
public class PayCalcPageController {

    private final PayCalcStatusService statusSvc;
    private final PayrollCalcService payrollCalcService;

    @GetMapping
    public String page(@RequestParam(name = "yyyymm", required = false) String yyyymm, Model model) {
        String mm = (yyyymm == null || yyyymm.isBlank())
                ? PayrollCalcService.currentYymm() : yyyymm;
        model.addAttribute("yyyymm", mm);
        model.addAttribute("status", statusSvc.getStatus(mm));
        return "pay/pay_calc_run";
    }

    @GetMapping("/status")
    @ResponseBody
    public PayCalcStatusDTO status(@RequestParam(name = "yyyymm") String yyyymm) {
        return statusSvc.getStatus(yyyymm);
    }

    /** ① 시뮬레이션 가계산 (DB에 가계산 결과 저장 — calcType=SIMULATED) */
    @PostMapping("/simulate")
    public String simulate(@RequestParam(name = "yyyymm") String yyyymm,
                           @RequestParam(name = "overwrite", required = false, defaultValue = "true") boolean overwrite,
                           RedirectAttributes ra) {
        int cnt = payrollCalcService.simulateMonthly(yyyymm, overwrite);
        ra.addFlashAttribute("msg", "가계산 완료: " + yyyymm + " (" + cnt + "건)");
        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }

    /** ② 확정 (필요 시 재계산 후 확정 — calcType=CALCULATED, status=CONFIRMED) */
    @PostMapping("/confirm")
    public String confirm(@RequestParam(name = "yyyymm") String yyyymm,
                          @RequestParam(name = "overwrite", required = false, defaultValue = "true") boolean overwrite,
                          RedirectAttributes ra) {
        int cnt = payrollCalcService.confirmMonthly(yyyymm, overwrite);
        ra.addFlashAttribute("msg", "확정 완료: " + yyyymm + " (" + cnt + "건)");
        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }
}
