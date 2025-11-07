package com.yeoun.pay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.repository.PayCalcRuleRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/pay/rule/calc")
@RequiredArgsConstructor
public class PayCalcRuleController {

    private final PayCalcRuleRepository payCalcRuleRepository;

    @PostMapping
    public String create(@ModelAttribute PayCalcRule payCalcRule, RedirectAttributes ra) {
        try {
        	payCalcRuleRepository.save(payCalcRule);
            ra.addFlashAttribute("msg", "계산규칙이 등록되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "계산규칙 등록 중 오류가 발생했습니다.");
            ra.addFlashAttribute("openCreateCalcModal", true);
            ra.addFlashAttribute("newCalc", payCalcRule);
        }
        return "redirect:/pay/rule";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute PayCalcRule payCalcRule,
                         RedirectAttributes ra) {
        try {
        	payCalcRule.setRuleId(id); // 엔티티 PK명에 맞게
            payCalcRuleRepository.save(payCalcRule);
            ra.addFlashAttribute("msg", "계산규칙이 수정되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "계산규칙 수정 중 오류가 발생했습니다.");
            ra.addFlashAttribute("openEditCalcModalId", id);
        }
        return "redirect:/pay/rule";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try { payCalcRuleRepository.deleteById(id); ra.addFlashAttribute("msg", "계산규칙이 삭제되었습니다."); }
        catch (Exception e) { ra.addFlashAttribute("err", "계산규칙 삭제 중 오류가 발생했습니다."); }
        return "redirect:/pay/rule";
    }
}
