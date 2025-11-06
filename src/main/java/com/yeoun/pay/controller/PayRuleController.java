package com.yeoun.pay.controller;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.service.PayRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/pay/rule")
@RequiredArgsConstructor
@Log4j2
public class PayRuleController {

    private final PayRuleService payRuleService;

    // [1] 급여 기준정보 메인 페이지
    @GetMapping
    public String listPage(Model model) {
        model.addAttribute("rules", payRuleService.findAll());
        model.addAttribute("newRule", new PayRule()); // ★ 폼 바인딩 객체
        return "pay/pay_rule";
    }


    // [2] 등록 처리
    @PostMapping
    public String create(@Valid @ModelAttribute("newRule") PayRule rule,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("rules", payRuleService.findAll());
            return "pay/pay_rule";
        }
        payRuleService.save(rule);
        return "redirect:/pay/rule";
    }

    // [3] 수정 처리
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("rule") PayRule updated,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("rules", payRuleService.findAll());
            return "pay/pay_rule";
        }
        payRuleService.update(id, updated);
        return "redirect:/pay/rule";
    }

    // [4] 삭제
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        payRuleService.delete(id);
        return "redirect:/pay/rule";
    }
}
