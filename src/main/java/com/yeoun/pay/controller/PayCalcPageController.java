package com.yeoun.pay.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.dto.PayslipViewDTO;
import com.yeoun.pay.repository.PayrollPayslipRepository;
import com.yeoun.pay.service.PayCalcStatusService;
import com.yeoun.pay.service.PayrollCalcQueryService;
import com.yeoun.pay.service.PayrollCalcService;

import lombok.RequiredArgsConstructor;

/**
 * [급여 계산 페이지 컨트롤러]
 * - /pay/calc : 계산 현황 페이지
 * - /pay/calc/simulate : 가계산(시뮬레이션)
 * - /pay/calc/confirm : 실제 확정 처리
 */
@Controller
@RequestMapping("/pay/calc")
@RequiredArgsConstructor
public class PayCalcPageController {

    private final PayCalcStatusService statusSvc;         // 현재 계산 상태 조회
    private final PayrollCalcService payrollCalcService;  // 급여 계산 로직
    private final PayrollCalcQueryService querySvc;       // 조회용 서비스
    private final PayrollPayslipRepository payslipRepo;   // 명세서 Repository


    /** ✅ AJAX 상태 조회 (JSON) */
    @GetMapping("/status")
    @ResponseBody
    public PayCalcStatusDTO status(@RequestParam("yyyymm") String yyyymm) {
        return statusSvc.getStatus(yyyymm);
    }


    /** ✅ [GET] 급여 계산 메인 페이지 */
    @GetMapping
    public String page(@RequestParam(name = "yyyymm", required = false) String yyyymm,
                       Model model) {

        // ① 파라미터 없으면 현재 년월 기본값
        String mm = (yyyymm == null || yyyymm.isBlank())
                ? PayrollCalcService.currentYymm() : yyyymm;

        // ② 급여 명세서 조회
        List<PayslipViewDTO> slips = querySvc.findForView(mm);

        // ③ 합계 계산
        var totals = querySvc.totals(slips);

        // ④ 모델 바인딩
        model.addAttribute("yyyymm", mm);
        model.addAttribute("status", statusSvc.getStatus(mm));
        model.addAttribute("slips", slips);
        model.addAttribute("sumPay", totals[0]);
        model.addAttribute("sumDed", totals[1]);
        model.addAttribute("sumNet", totals[2]);

        return "pay/pay_calc_run"; // ✅ 통일된 뷰 반환
    }


    /** ✅ [POST] 가계산 (시뮬레이션) */
    @PostMapping("/simulate")
    public String simulate(@RequestParam(name="yyyymm") String yyyymm,
                           @RequestParam(name="overwrite", defaultValue="true") boolean overwrite,
                           RedirectAttributes ra) {

        int cnt = payrollCalcService.simulateMonthly(yyyymm, overwrite);
        ra.addFlashAttribute("msg", "가계산 완료: " + yyyymm + " (" + cnt + "건)");

        return "redirect:/pay/calc?yyyymm=" + yyyymm; // ✅ 다시 GET으로 리다이렉트
    }


    /** ✅ [POST] 실제 확정 처리 */
    @PostMapping("/confirm")
    public String confirm(
            @RequestParam(name = "yyyymm") String yyyymm,
            @RequestParam(name = "overwrite", defaultValue = "true") boolean overwrite,
            RedirectAttributes ra) {

        int cnt = payrollCalcService.confirmMonthly(yyyymm, overwrite, "SYSTEM");
        ra.addFlashAttribute("msg", "급여 확정 완료: " + yyyymm + " (" + cnt + "건)");

        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }

}
