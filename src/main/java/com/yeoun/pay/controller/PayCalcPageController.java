// src/main/java/com/yeoun/pay/controller/PayCalcPageController.java
package com.yeoun.pay.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.service.PayCalcStatusService;
import com.yeoun.pay.service.PayrollCalcQueryService;
import com.yeoun.pay.service.PayrollCalcService;

import lombok.RequiredArgsConstructor;

/**
 * [급여 계산 페이지 컨트롤러]
 * - /pay/calc : 계산 현황 페이지
 * - /pay/calc/simulate : 가계산(시뮬레이션) 실행
 * - /pay/calc/confirm : 실제 확정 처리
 */
@Controller
@RequestMapping("/pay/calc")
@RequiredArgsConstructor
public class PayCalcPageController {

    private final PayCalcStatusService statusSvc;         // 현재 계산 상태 조회 (예: READY, CALCULATED, CONFIRMED)
    private final PayrollCalcService payrollCalcService;  // 급여 계산(비즈니스 로직 담당)
    private final PayrollCalcQueryService querySvc;       // 계산결과 조회용 서비스
    
    /** AJAX 상태 조회용 (JSON 응답) */
    @GetMapping("/status")
    @ResponseBody   
    public PayCalcStatusDTO status(@RequestParam("yyyymm") String yyyymm) {
        return statusSvc.getStatus(yyyymm);
    }


    /**
     * [GET] 급여 계산 메인 페이지
     * - 기본 월(yyyymm) 기준으로 계산 상태/명세서/합계 표시
     */
    @GetMapping
    public String page(@RequestParam(name = "yyyymm", required = false) String yyyymm,
                       Model model) {

        // ① 파라미터 없으면 현재 년월로 기본 설정
        String mm = (yyyymm == null || yyyymm.isBlank())
                ? PayrollCalcService.currentYymm() : yyyymm;

        // ② 급여 명세서 리스트 조회
        var slips = querySvc.findForView(mm);

        // ③ 합계 계산 (지급/공제/실수령)
        var totals = querySvc.totals(slips);

        // ④ 화면 모델 바인딩
        model.addAttribute("yyyymm", mm);
        model.addAttribute("status", statusSvc.getStatus(mm)); // 현재 월의 계산상태 (CalcStatus)
        model.addAttribute("slips", slips);
        model.addAttribute("sumPay", totals[0]);
        model.addAttribute("sumDed", totals[1]);
        model.addAttribute("sumNet", totals[2]);

        // ⑤ Thymeleaf 템플릿 반환
        return "pay/pay_calc_run";
    }

    /**
     * [POST] 가계산 실행 (시뮬레이션)
     * - 이미 계산된 데이터가 있으면 덮어쓸지 여부(overwrite) 선택 가능
     * - PayrollCalcService.simulateMonthly() 호출
     */
    @PostMapping("/simulate")
    public String simulate(@RequestParam(name="yyyymm") String yyyymm,
                           @RequestParam(name="overwrite", defaultValue="true") boolean overwrite,
                           RedirectAttributes ra) {
        int cnt = payrollCalcService.simulateMonthly(yyyymm, overwrite);

        // 화면 알림용 메시지
        ra.addFlashAttribute("msg", "가계산 완료: " + yyyymm + " (" + cnt + "건)");
        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }

    /**
     * [POST] 실제 확정 처리
     * - PayrollCalcService.confirmMonthly() 호출
     * - confirmMonthly 내부에서 calcStatus를 CONFIRMED로 업데이트
     */
    @PostMapping("/confirm")
    public String confirm(@RequestParam(name = "yyyymm") String yyyymm,
                          @RequestParam(name = "overwrite", defaultValue = "true") boolean overwrite,
                          RedirectAttributes ra,
                          Principal principal) {

        // ① 로그인 사용자 ID 추출
        String userId = principal != null ? principal.getName() : "SYSTEM";

        // ② 급여 확정 처리
        int cnt = payrollCalcService.confirmMonthly(yyyymm, overwrite, userId);

        // ③ 결과 메시지 및 리다이렉트
        ra.addFlashAttribute("msg", "확정 완료: " + yyyymm + " (" + cnt + "건)");
        return "redirect:/pay/calc?yyyymm=" + yyyymm;
    }

}
