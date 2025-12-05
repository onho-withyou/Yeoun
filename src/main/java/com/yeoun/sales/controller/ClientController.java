package com.yeoun.sales.controller;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.MaterialMst;
import com.yeoun.masterData.repository.MaterialMstRepository;
import com.yeoun.sales.dto.ClientItemDTO;
import com.yeoun.sales.entity.Client;
import com.yeoun.sales.service.ClientItemService;
import com.yeoun.sales.service.ClientService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final ClientItemService itemService;
    private final MaterialMstRepository materialRepository;  


    /* ======================================================
       1. 거래처/협력사 목록 페이지 (HTML)
    ====================================================== */
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


    /* ======================================================
       2. 목록 JSON API (AG Grid 전용)
    ====================================================== */
    @GetMapping("/data")
    @ResponseBody
    public List<Client> listData(
            @RequestParam(value="keyword", required = false) String keyword,
            @RequestParam(value="type", required = false, defaultValue = "CUSTOMER") String type
    ) {
        return clientService.search(keyword, type);
    }


    /* ======================================================
       3. 상세 페이지 (CUSTOMER / SUPPLIER HTML 분리)
    ====================================================== */
    @GetMapping("/{clientId}")
    public String detailPage(
            @PathVariable("clientId") String clientId,
            Model model,
            RedirectAttributes rttr
    ) {
        Client client = clientService.get(clientId);

        if (client == null) {
            rttr.addFlashAttribute("msg", "존재하지 않는 거래처입니다.");
            return "redirect:/sales/client";
        }

        model.addAttribute("client", client);

        // ▶ CUSTOMER → 고객 상세 페이지
        if ("CUSTOMER".equalsIgnoreCase(client.getClientType())) {
            return "sales/client_detail";
        }

        // ▶ SUPPLIER → 협력사 상세 페이지
        model.addAttribute("items", itemService.getItems(clientId));
        model.addAttribute("materials", materialRepository.findAll());
        
        return "sales/supplier_detail";
    }


    /* ======================================================
       4. 상세정보 JSON API
    ====================================================== */
    @GetMapping("/detail/{clientId}")
    @ResponseBody
    public Client detailData(@PathVariable("clientId") String clientId) {
        return clientService.get(clientId);
    }


    /* ======================================================
       5. 거래처 등록 페이지
    ====================================================== */
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("client", new Client());
        return "sales/client_create";
    }


    /* ======================================================
       6. 거래처 등록 프로세스
    ====================================================== */
    @PostMapping("/create")
    public String createProcess(
            @ModelAttribute Client client,
            RedirectAttributes rttr
    ) {
        try {
            clientService.create(client);
            rttr.addFlashAttribute("msg", "거래처가 등록되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "오류: " + e.getMessage());
            return "redirect:/sales/client/create";
        }

        return "redirect:/sales/client";
    }


    /* ======================================================
       7. 사업자번호 중복 확인
    ====================================================== */
    @GetMapping("/check-business")
    @ResponseBody
    public boolean checkBusiness(@RequestParam("businessNo") String businessNo) {

        String cleanBiz = businessNo.replaceAll("[^0-9]", ""); // 숫자만 남기는 정규식

        return !clientService.existsByBusinessNoClean(cleanBiz);
        // true = 사용 가능 / false = 중복
    }
    
    
    /* ======================================================
    8. 협력사 취급제품
 ====================================================== */
    
    @PostMapping("/{clientId}/items")
    @ResponseBody
    public String saveItems(
            @PathVariable String clientId,
            @RequestBody List<ClientItemDTO> items,
            @AuthenticationPrincipal LoginDTO login
    ) {
        itemService.addItems(clientId, items, login.getEmpId());
        return "OK";
    }
    
    @GetMapping("/material/data")
    @ResponseBody
    public List<MaterialMst> getMaterialList() {
        return materialRepository.findAll();
    }



}
