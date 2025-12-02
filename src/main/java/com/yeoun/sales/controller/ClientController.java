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
    @GetMapping("/{clientId}")
    @ResponseBody
    public Client detail(@PathVariable String clientId) {
        return clientService.get(clientId);
    }


}
