package com.yeoun.pay.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.service.PayCalcRuleService; // **Service Layer 추가**

@Controller
@RequestMapping("/pay/rule_calc")
@RequiredArgsConstructor
@Log4j2
public class PayCalcRuleController {

    // Repository 대신 Service 주입
    private final PayCalcRuleService payCalcRuleService; 

    /** 계산 규칙 등록 (CREATE) */
    @PostMapping // POST: /pay/rule_calc
    public String create(@Valid @ModelAttribute("newCalc") PayCalcRule form,
                         BindingResult br,
                         RedirectAttributes ra) {
        
        if (br.hasErrors()) {
            log.warn("create() validation errors: {}", br.getAllErrors());
            ra.addFlashAttribute("err", "입력값을 확인하세요.");
            ra.addFlashAttribute("newCalc", form);
            ra.addFlashAttribute("openCreateCalcModal", true);
            return "redirect:/pay/rule_calc";
        }
        
        try {
            // Service 호출로 변경
            payCalcRuleService.save(form); 
            ra.addFlashAttribute("msg", "계산규칙이 등록되었습니다.");
            
        } catch (IllegalArgumentException iae) {
            // ✅ 여기서 잡아서 모달 열고 값 유지
            log.error("create() biz validation error: {}", iae.getMessage());
            ra.addFlashAttribute("err", iae.getMessage()); // ex) item(ITEM_CODE)은 필수입니다.
            ra.addFlashAttribute("newCalc", form);
            ra.addFlashAttribute("openCreateCalcModal", true);
        } catch (Exception e) {
            log.error("create() error", e);
            ra.addFlashAttribute("err", "계산규칙 등록 중 오류가 발생했습니다.");
            ra.addFlashAttribute("newCalc", form);
            ra.addFlashAttribute("openCreateCalcModal", true);
        }
        return "redirect:/pay/rule_calc";
    }

    /** 계산 규칙 수정 (UPDATE) */
    @PutMapping("/{id}") // **HTTP Method를 PUT으로 변경**
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("newCalc") PayCalcRule form,
                         BindingResult br,
                         RedirectAttributes ra) {
        
        if (br.hasErrors()) {
            log.warn("update() validation errors: {}", br.getAllErrors());
            ra.addFlashAttribute("err", "입력값을 확인하세요.");
            ra.addFlashAttribute("openEditCalcModalId", id);
            ra.addFlashAttribute("newCalc", form);
            return "redirect:/pay/rule_calc";
        }
        
        try {
            form.setRuleId(id); // PK 설정
            // Service 호출로 변경
            payCalcRuleService.save(form); 
            ra.addFlashAttribute("msg", "계산규칙이 수정되었습니다.");
            
        } catch (DataIntegrityViolationException e) {
            log.error("update() DataIntegrityViolationException", e);
            ra.addFlashAttribute("err", "필수 항목 누락 또는 중복된 값이 있습니다. (DB 제약 조건 오류)");
            ra.addFlashAttribute("openEditCalcModalId", id);
            ra.addFlashAttribute("newCalc", form);
        } catch (Exception e) {
            log.error("update() error", e);
            ra.addFlashAttribute("err", "계산규칙 수정 중 일반적인 오류가 발생했습니다.");
            ra.addFlashAttribute("openEditCalcModalId", id);
            ra.addFlashAttribute("newCalc", form);
        }
        
        return "redirect:/pay/rule_calc";
    }

    /** 계산 규칙 삭제 (DELETE) */
    @DeleteMapping("/{id}") // **HTTP Method를 DELETE로 변경 (URL도 단순화)**
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            // Service 호출로 변경
            payCalcRuleService.delete(id); 
            ra.addFlashAttribute("msg", "계산규칙이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("delete() error", e);
            ra.addFlashAttribute("err", "계산규칙 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/pay/rule_calc";
    }
}