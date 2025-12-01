package com.yeoun.sales.controller;

import com.yeoun.sales.entity.Client;
import com.yeoun.sales.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales/client")
public class ClientController {

    private final ClientService clientService;

    /** 목록페이지 */
    @GetMapping
    public String list(@RequestParam(value="keyword",required = false) String keyword,
                       @RequestParam(value="keyword",required = false) String type,
                       Model model) {
    	

        List<Client> list = clientService.search(keyword, type);
        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);

        return "sales/client_list";
    }

    /** 목록 JSON API */
    @GetMapping("/data")
    @ResponseBody
    public List<Client> listData(@RequestParam(value="keyword",required = false) String keyword,
                                 @RequestParam(value="type",required = false) String type) {
        return clientService.search(keyword, type);
    }

    /** 상세조회 (PathVariable → RequestParam 방식으로 변경) */
    @GetMapping("/detail")
    @ResponseBody
    public Client detail(@RequestParam("clientId") String clientId) {
        return clientService.get(clientId);
    }

    /** 등록 */
    @PostMapping("/create")
    @ResponseBody
    public String create(@ModelAttribute Client form) {
        clientService.create(form);
        return "OK";   // 반드시 OK
    }

    /** 수정 */
    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestParam("clientId") String clientId,
                         @ModelAttribute Client form) {
        clientService.update(clientId, form);
        return "OK";   // 반드시 OK
    }
}
