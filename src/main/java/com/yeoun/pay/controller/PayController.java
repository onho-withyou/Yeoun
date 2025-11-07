package com.yeoun.pay.controller;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.service.PayRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pay")
@Log4j2
@RequiredArgsConstructor
public class PayController {

    // 조회에 필요한 의존성만 주입 (쓰기/검증은 각 도메인 컨트롤러에서)
    private final PayRuleService payRuleService;
    private final PayItemMstRepository payItemMstRepository;
    private final PayCalcRuleRepository payCalcRuleRepository;

    /** 급여 기준정보 허브 페이지 (조회 전용) */
    @GetMapping("/rule")
    public String ruleHubPage(
            Model model,
            @RequestParam(value = "msg", required = false) String msg,
            @RequestParam(value = "err", required = false) String err
    ) {
        // 1) 각 테이블 목록 조회 (연관필요 없음)
        model.addAttribute("rules",     payRuleService.findAll());
        model.addAttribute("items",     payItemMstRepository.findAllByOrderBySortNoAsc());
        model.addAttribute("calcRules", payCalcRuleRepository.findAllByOrderByPriorityAsc());

        // 2) 신규 등록 폼 바인딩 객체(유효성 실패로 플래시에서 넘어온 게 없을 때만 준비)
        if (!model.containsAttribute("newRule")) {
            model.addAttribute("newRule", new PayRule());
        }

        // 3) 플래시 메시지 표시
        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);

        return "pay/pay_rule";  // 타임리프 템플릿 하나에서 3 블록 모두 렌더링
    }

    // 급여 명세서 페이지
    @GetMapping("/emp_pay")
    public String empPay() {
        return "pay/emp_pay";
    }

    @GetMapping("/payroll_payslip")
    public String payRunPage() {
        return "pay/payroll_payslip";
    }
}
