package com.yeoun.pay.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.dto.PayslipDetailDTO;
import com.yeoun.pay.dto.PayslipViewDTO;
import com.yeoun.pay.repository.EmpNativeRepository;
import com.yeoun.pay.repository.PayrollPayslipRepository;
import com.yeoun.pay.service.PayCalcStatusService;
import com.yeoun.pay.service.PayrollCalcQueryService;
import com.yeoun.pay.service.PayrollCalcService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


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
    private final PayrollCalcQueryService payrollCalcQueryService;
    private final EmpNativeRepository empNativeRepository;
   
    

    /** ✅ AJAX 상태 조회 (JSON) */
    @GetMapping("/status")
    @ResponseBody
    public PayCalcStatusDTO status(@RequestParam("yyyymm") String yyyymm) {
        return statusSvc.getStatus(yyyymm);
    }


    /** ✅ [GET] 급여 계산 메인 페이지 */
    @GetMapping
    public String page(@RequestParam(name="yyyymm", required=false) String yyyymm,
                       Model model) {

        String mm = (yyyymm == null || yyyymm.isBlank())
                ? PayrollCalcService.currentYymm()
                : yyyymm;

        List<PayslipViewDTO> slips = querySvc.findForView(mm);

        var totals = querySvc.totals(slips);

        model.addAttribute("yyyymm", mm);
        model.addAttribute("status", statusSvc.getStatus(mm)); // ✔ 정상 작동!
        model.addAttribute("slips", slips);
        model.addAttribute("sumPay", totals[0]);
        model.addAttribute("sumDed", totals[1]);
        model.addAttribute("sumNet", totals[2]);

        return "pay/pay_calc_run";
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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empId = auth.getName();  // 로그인 사번

        // 사번으로 이름 조회
        String empName = empNativeRepository.findEmpNameByEmpId(empId);

        int cnt = payrollCalcService.confirmMonthly(yyyymm, overwrite, empName);

        ra.addFlashAttribute("msg", empName + " 님이 급여 확정 완료했습니다.");

        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }



    /** 급여 상세 조회 (AJAX) */
    @GetMapping("/detail")
    @ResponseBody
    public PayslipDetailDTO getPayslipDetail(
            @RequestParam("yyyymm") String yyyymm,
            @RequestParam("empId") String empId) {

        return payrollCalcQueryService.getPayslipDetail(yyyymm, empId);
    }

}
