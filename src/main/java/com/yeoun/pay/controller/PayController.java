package com.yeoun.pay.controller;

import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.entity.PayRule;
import com.yeoun.pay.repository.PayCalcRuleRepository;
import com.yeoun.pay.repository.PayItemMstRepository;
import com.yeoun.pay.service.PayRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
@Log4j2
public class PayController {

    private final PayRuleService payRuleService;
    private final PayItemMstRepository payItemMstRepository;
    private final PayCalcRuleRepository payCalcRuleRepository;

    /** 급여기준정보 페이지 */
    @GetMapping("/rule")
    public String rulePage(Model model,
                           @RequestParam(value="msg", required=false) String msg,
                           @RequestParam(value="err", required=false) String err) {
        model.addAttribute("activeTab", "rule");
        model.addAttribute("rules", payRuleService.findAll());
        if (!model.containsAttribute("newRule")) model.addAttribute("newRule", new PayRule());
        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);
        return "pay/pay_rule";
    }
    
    // --- 급여항목 페이지 수정 시작 (검색/메시지 처리 포함) ---
    /** 급여항목 페이지 */
    @GetMapping("/rule_item")
    public String itemPage(Model model,
                           @RequestParam(value = "msg", required = false) String msg,
                           @RequestParam(value = "err", required = false) String err,
                           @RequestParam Map<String, String> params) {

        model.addAttribute("activeTab", "item");

        // 목록(비페이지네이션) — 템플릿의 items 블록과 매칭
        model.addAttribute("items", payItemMstRepository.findAllByOrderBySortNoAsc());

        // 등록 모달 바인딩 객체 — 템플릿의 th:object="${item}"와 매칭
        if (!model.containsAttribute("item")) {
            model.addAttribute("item", new PayItemMst()); // DTO 쓰면 new PayItemForm()으로 교체
        }

        // 검색 파라미터(셀렉트 유지/입력값 유지용)
        model.addAttribute("params", params);

        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);

        return "pay/pay_item";
    }

    // --- 급여항목 페이지 수정 끝 ---

    /** 급여계산 페이지 */
    @GetMapping("/rule_calc")
    public String calcPage(Model model,
                           @RequestParam(value="msg", required=false) String msg,
                           @RequestParam(value="err", required=false) String err) {
        model.addAttribute("activeTab", "calc");
        model.addAttribute("calcRules", payCalcRuleRepository.findAllByOrderByPriorityAsc());
        
        // 메시지 및 에러 파라미터 처리 추가
        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);

        // 등록/수정 폼에 필요한 바인딩 객체 추가 (예: 새 계산 규칙 객체)
        if (!model.containsAttribute("newCalcRule")) {
            // PayCalcRule 엔티티가 있다고 가정하고 추가
            model.addAttribute("newCalcRule", new com.yeoun.pay.entity.PayCalcRule()); 
        }

        return "pay/pay_calc";
    }
}