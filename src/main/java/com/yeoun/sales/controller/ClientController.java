package com.yeoun.sales.controller;

import com.yeoun.sales.entity.Client;
import com.yeoun.sales.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales/client")
public class ClientController {

    private final ClientService clientService;

    /** 목록페이지 */
    @GetMapping
    public String list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false, defaultValue = "CUSTOMER") String type,
            Model model
    ) {

        List<Client> list = clientService.search(keyword, type);

        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);

        return "sales/client_list";
    }


    /** 목록 JSON API */
    @GetMapping("/data")
    @ResponseBody
    public List<Client> listData(
            @RequestParam(value="keyword",required = false) String keyword,
            @RequestParam(value="type",required = false, defaultValue = "CUSTOMER") String type
    ) {
        return clientService.search(keyword, type);
    }

    /**상세조회*/    
    @GetMapping("/detail/{clientId}")
    @ResponseBody
    public Client detail(@PathVariable("clientId") String clientId) {
        return clientService.get(clientId);
    }


    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("client", new Client());
        return "sales/client_create";
    }

    /**거래처 등록*/
    @PostMapping("/create")
    public String createProcess(@ModelAttribute Client client, RedirectAttributes rttr) {

        try {
            clientService.create(client);
            rttr.addFlashAttribute("msg", "거래처가 등록되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "오류: " + e.getMessage());
            return "redirect:/sales/client/create";
        }

        return "redirect:/sales/client";
    }

    /** 사업자번호 중복 확인 */
    @GetMapping("/check-business")
    @ResponseBody
    public boolean checkBusiness(@RequestParam("businessNo") String businessNo) {
        String cleanBiz = businessNo.replaceAll("[^0-9]", "");
        return !clientService.existsByBusinessNoClean(cleanBiz);
    }

    
    

}

