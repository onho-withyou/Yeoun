package com.yeoun.pay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.service.PayrollCalcService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/pay/run")
@RequiredArgsConstructor
@Log4j2
public class PayRunController {

    private final PayrollCalcService calcService;

    /** 화면에서 "급여계산 실행" 버튼이 누르는 엔드포인트 */
    @PostMapping("/batch")
    public String runBatch(@RequestParam(required = false) String yyyymm,
                           @RequestParam(defaultValue = "false") boolean overwrite,
                           @RequestParam(defaultValue = "false") boolean simulate,
                           RedirectAttributes ra) {
        String payYymm = (yyyymm == null || yyyymm.isBlank())
                ? PayrollCalcService.currentYymm() : yyyymm;

        int affected = calcService.runMonthlyBatch(payYymm, overwrite, null, simulate, null);
        ra.addFlashAttribute("msg", String.format("%s 급여계산 완료: %d건",
                payYymm, affected));
        return "redirect:/pay/rule"; // 계산 후 돌아갈 화면
    }
}
