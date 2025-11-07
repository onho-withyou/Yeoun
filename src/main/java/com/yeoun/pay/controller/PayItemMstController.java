package com.yeoun.pay.controller;

import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.repository.PayItemMstRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/pay/rule/item")
@RequiredArgsConstructor
@Log4j2
public class PayItemMstController {

    private final PayItemMstRepository payItemMstRepository ;

    @PostMapping           // 등록
    public String create(@Valid @ModelAttribute("item") PayItemMst item,
                         BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("err", "항목 입력값을 확인해주세요.");
            ra.addFlashAttribute("openItemCreate", true);
            return "redirect:/pay/rule"; // 같은 메인 페이지로
        }
        if (payItemMstRepository.existsByItemCode(item.getItemCode())) {
            ra.addFlashAttribute("err", "중복된 ITEM_CODE 입니다.");
            ra.addFlashAttribute("openItemCreate", true);
            return "redirect:/pay/rule";
        }
        payItemMstRepository.save(item);
        ra.addFlashAttribute("msg", "항목이 등록되었습니다.");
        return "redirect:/pay/rule";
    }

    @PostMapping("/{itemCode}") // 수정
    public String update(@PathVariable String itemCode,
                         @Valid @ModelAttribute("item") PayItemMst form,
                         BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("err", "항목 입력값을 확인해주세요.");
            ra.addFlashAttribute("openItemEdit", itemCode);
            return "redirect:/pay/rule";
        }
        // PK는 변경하지 않음
        form.setItemCode(itemCode);
        payItemMstRepository.save(form);
        ra.addFlashAttribute("msg", "항목이 수정되었습니다.");
        return "redirect:/pay/rule";
    }

    @GetMapping("/delete/{itemCode}") // 삭제
    public String delete(@PathVariable String itemCode, RedirectAttributes ra) {
        try {
        	payItemMstRepository.deleteById(itemCode);
            ra.addFlashAttribute("msg", "항목이 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/pay/rule";
    }
}
