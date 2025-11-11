package com.yeoun.pay.controller;

import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.enums.ActiveStatus;
import com.yeoun.pay.service.PayRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pay/rule")
@RequiredArgsConstructor
@Log4j2
public class PayRuleController {

    private final PayRuleService payRuleService;

    /** 등록 (모달) : 항상 redirect */
    @PostMapping
    public String create(@Valid @ModelAttribute("newRule") PayRule form,
                         BindingResult br,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            ra.addFlashAttribute("err", "입력값을 확인하세요.");
            ra.addFlashAttribute("newRule", form);
            ra.addFlashAttribute("openCreateModal", true);
            return "redirect:/pay/rule";
        }

        try {
            payRuleService.save(form);
            ra.addFlashAttribute("msg", "등록되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 비즈니스 예외(기간겹침 등)
            log.warn("create() validation error: {}", e.getMessage());
            ra.addFlashAttribute("err", e.getMessage());
            ra.addFlashAttribute("newRule", form);
            ra.addFlashAttribute("openCreateModal", true);
        } catch (Exception e) {
            log.error("create() error", e);
            ra.addFlashAttribute("err", "등록 중 오류가 발생했습니다.");
            ra.addFlashAttribute("newRule", form);
            ra.addFlashAttribute("openCreateModal", true);
        }
        return "redirect:/pay/rule";
    }

    /** 수정 (모달) : 항상 redirect */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("newRule") PayRule form, // 템플릿 바인딩명 통일
                         BindingResult br,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            ra.addFlashAttribute("err", "입력값을 확인하세요.");
            ra.addFlashAttribute("newRule", form);
            ra.addFlashAttribute("openEditModalId", id);
            return "redirect:/pay/rule";
        }

        try {
            form.setRuleId(id);
            payRuleService.update(id, form);
            ra.addFlashAttribute("msg", "수정되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("update() validation error: {}", e.getMessage());
            ra.addFlashAttribute("err", e.getMessage());
            ra.addFlashAttribute("newRule", form);
            ra.addFlashAttribute("openEditModalId", id);
        } catch (Exception e) {
            log.error("update() error", e);
            ra.addFlashAttribute("err", "수정 중 오류가 발생했습니다.");
            ra.addFlashAttribute("newRule", form);
            ra.addFlashAttribute("openEditModalId", id);
        }
        return "redirect:/pay/rule";
    }

    /** 상태 변경 (예: ACTIVE/INACTIVE) : 항상 redirect */
    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam("value") ActiveStatus value,
                               RedirectAttributes ra) {
        try {
            payRuleService.changeStatus(id, value);
            ra.addFlashAttribute("msg", "상태가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("err", e.getMessage());
        } catch (Exception e) {
            log.error("changeStatus() error", e);
            ra.addFlashAttribute("err", "상태 변경 중 오류가 발생했습니다.");
        }
        return "redirect:/pay/rule";
    }

    /** 삭제는 POST 권장 */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            payRuleService.delete(id);
            ra.addFlashAttribute("msg", "삭제되었습니다.");
        } catch (Exception e) {
            log.error("delete() error", e);
            ra.addFlashAttribute("err", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/pay/rule";
    }
}
