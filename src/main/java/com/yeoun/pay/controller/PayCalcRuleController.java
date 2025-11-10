package com.yeoun.pay.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.repository.PayCalcRuleRepository;

@Controller
@RequestMapping("/pay/rule_calc")
@RequiredArgsConstructor
@Log4j2
public class PayCalcRuleController {

    private final PayCalcRuleRepository payCalcRuleRepository;

    /** 계산 규칙 등록 */
    @PostMapping
    public String create(@Valid @ModelAttribute("newCalc") PayCalcRule form,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            log.warn("create() validation errors: {}", br.getAllErrors());
            ra.addFlashAttribute("err", "입력값을 확인하세요.");
            ra.addFlashAttribute("newCalc", form);           // 폼 값 유지
            ra.addFlashAttribute("openCreateCalcModal", true);
            return "redirect:/pay/rule_calc";
        }
        try {
            payCalcRuleRepository.save(form);
            ra.addFlashAttribute("msg", "계산규칙이 등록되었습니다.");
        } catch (Exception e) {
            log.error("create() error", e);
            ra.addFlashAttribute("err", "계산규칙 등록 중 오류가 발생했습니다.");
            ra.addFlashAttribute("newCalc", form);
            ra.addFlashAttribute("openCreateCalcModal", true);
        }
        return "redirect:/pay/rule_calc";
    }

    /** 계산 규칙 수정 */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("newCalc") PayCalcRule form, // ★ 이름 통일
                         BindingResult br,                                   // ★ 위치 OK
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            log.warn("update() validation errors: {}", br.getAllErrors());
            ra.addFlashAttribute("err", "입력값을 확인하세요.");
            ra.addFlashAttribute("openEditCalcModalId", id);
            ra.addFlashAttribute("newCalc", form);     // ★ 값 유지
            return "redirect:/pay/rule_calc";
        }
        try {
            form.setRuleId(id); // PK는 PathVariable로 고정
            payCalcRuleRepository.save(form);
            ra.addFlashAttribute("msg", "계산규칙이 수정되었습니다.");
        } catch (Exception e) {
            log.error("update() error", e);
            ra.addFlashAttribute("err", "계산규칙 수정 중 오류가 발생했습니다.");
            ra.addFlashAttribute("openEditCalcModalId", id);
            ra.addFlashAttribute("newCalc", form);     // ★ 값 유지
        }
        return "redirect:/pay/rule_calc";
    }


    /** 계산 규칙 삭제 */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            payCalcRuleRepository.deleteById(id);
            ra.addFlashAttribute("msg", "계산규칙이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("delete() error", e);
            ra.addFlashAttribute("err", "계산규칙 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/pay/rule_calc";
    }
}
