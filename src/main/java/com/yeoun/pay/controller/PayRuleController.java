package com.yeoun.pay.controller;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.service.PayRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pay/rule")
@RequiredArgsConstructor
@Log4j2
public class PayRuleController {

    private final PayRuleService payRuleService;
    private final PayItemMstRepository payItemMstRepository;
    private final PayCalcRuleRepository payCalcRuleRepository;

//    /** [1] 급여 기준정보 메인 페이지 */
//    @GetMapping
//    public String listPage(Model model,
//                           @RequestParam(value = "msg", required = false) String flashMsg,
//                           @RequestParam(value = "err", required = false) String flashErr) {
//        model.addAttribute("rules", payRuleService.findAll());
//        // 새 등록 폼 바인딩 객체가 없으면 생성 (검증 실패 시 기존 값 유지)
//        if (!model.containsAttribute("newRule")) {
//            model.addAttribute("newRule", new PayRule());
//        }
//        // 플래시 메시지 전달
//        if (flashMsg != null) model.addAttribute("msg", flashMsg);
//        if (flashErr != null) model.addAttribute("err", flashErr);
//        return "pay/pay_rule";
//    }

    /** [2] 등록 처리 (모달) */
    @PostMapping
    public String create(@Valid @ModelAttribute("newRule") PayRule rule,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            // 목록 + 모달 재오픈
            model.addAttribute("rules", payRuleService.findAll());
            model.addAttribute("openCreateModal", true);
            return "pay/pay_rule";
        }
        try {
            payRuleService.save(rule);
            ra.addFlashAttribute("msg", "등록되었습니다.");
            return "redirect:/pay/rule";
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 겹침/유효성 등 비즈니스 예외 -> 같은 화면에 메시지 + 모달 재오픈
            model.addAttribute("rules", payRuleService.findAll());
            model.addAttribute("openCreateModal", true);
            model.addAttribute("err", e.getMessage());
            return "pay/pay_rule";
        } catch (Exception e) {
            log.error("등록 오류", e);
            ra.addFlashAttribute("err", "적용기간 설정 오류가 발생했습니다.");
            return "redirect:/pay/rule";
        }
    }

    /** [3] 수정 처리 (모달) */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("rule") PayRule updated,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            // 목록 + 수정 모달 재오픈
            model.addAttribute("rules", payRuleService.findAll());
            model.addAttribute("newRule", new PayRule());
            model.addAttribute("openEditModalId", id);
            return "pay/pay_rule";
        }
        try {
            // 폼에 ID 필드가 없어도 안전하게 주입
            updated.setRuleId(id);
            payRuleService.update(id, updated);
            ra.addFlashAttribute("msg", "수정되었습니다.");
            return "redirect:/pay/rule";
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 겹침/유효성 등 비즈니스 예외 -> 같은 화면 + 수정 모달 재오픈
            model.addAttribute("rules", payRuleService.findAll());
            model.addAttribute("newRule", new PayRule());
            model.addAttribute("openEditModalId", id);
            model.addAttribute("err", e.getMessage());
            return "pay/pay_rule";
        } catch (Exception e) {
            log.error("수정 오류", e);
            ra.addFlashAttribute("err", "적용기간 설정 오류가 발생했습니다.");
            return "redirect:/pay/rule";
        }
    }

    /** [4] 삭제 처리 (GET 대신 POST/DELETE 권장) */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            payRuleService.delete(id);
            ra.addFlashAttribute("msg", "삭제되었습니다.");
        } catch (Exception e) {
            log.error("삭제 오류", e);
            ra.addFlashAttribute("err", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/pay/rule";
    }
}
