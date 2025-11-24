package com.yeoun.pay.controller;

import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.pay.entity.PayCalcRule;
import com.yeoun.pay.enums.ActiveStatus;
import com.yeoun.pay.enums.RuleType;
import com.yeoun.pay.enums.TargetType;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.service.PayCalcRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/pay/rule_calc")
@RequiredArgsConstructor
@Log4j2
public class PayCalcRuleController {

    private final PayCalcRuleService service;
    private final PayItemMstRepository itemRepo;
    private final DeptRepository deptRepo;
    private final PositionRepository positionRepo;

    /** 페이지 */
    @GetMapping
    public String page(Model model) {

        model.addAttribute("activeTab", "calc");
        model.addAttribute("newCalc", new PayCalcRule());

        model.addAttribute("calcRules", service.findAllOrderByPriority());
        model.addAttribute("items", itemRepo.findAll());
        model.addAttribute("ruleTypes", RuleType.values());
        model.addAttribute("targetTypes", TargetType.values());
        model.addAttribute("statuses", ActiveStatus.values());
        model.addAttribute("depts", deptRepo.findActive());    
        model.addAttribute("positions", positionRepo.findActive()); 



        // 등록폼 바인딩 객체
        if (!model.containsAttribute("createForm")) {
            PayCalcRule blank = PayCalcRule.builder()
                    .startDate(LocalDate.now())
                    .priority(100)
                    .status(ActiveStatus.ACTIVE)
                    .build();
            model.addAttribute("createForm", blank);
        }

        return "pay/pay_calc";
    }

    /** 등록 */
    @PostMapping("/create")
    public String create(@ModelAttribute("createForm") PayCalcRule form,
                         RedirectAttributes ra) {

        try {
            service.save(form);
            ra.addFlashAttribute("msg", "등록되었습니다.");

        } catch (Exception e) {
            ra.addFlashAttribute("createErrorMsg", e.getMessage());
            ra.addFlashAttribute("createForm", form);
            ra.addFlashAttribute("openCreateModal", true);
        }

        return "redirect:/pay/rule_calc";
    }

    /** 수정 */
    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Long id,
    				     @ModelAttribute("editForm") PayCalcRule form,
                         RedirectAttributes ra) {

        try {
            form.setRuleId(id);
            service.save(form);
            ra.addFlashAttribute("msg", "수정되었습니다.");

        } catch (Exception e) {

            // valueNum 파싱 문제, DB 오류 등 실제 저장 오류만 메시지 전달
            if (!e.getMessage().contains("targetCode") ) {
                ra.addFlashAttribute("editErrorMsg", e.getMessage());
            }

            ra.addFlashAttribute("openEditModalId", id);
        }


        return "redirect:/pay/rule_calc";
    }

    /** 삭제 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("msg", "삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "삭제 중 오류");
        }
        return "redirect:/pay/rule_calc";
    
    
    }       
    
}


