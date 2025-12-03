package com.yeoun.sales.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.sales.dto.OrderListDTO;
import com.yeoun.sales.entity.Client;
import com.yeoun.sales.service.ClientService;
import com.yeoun.sales.service.OrdersService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales/orders")
public class OrdersController {

    private final OrdersService ordersService;
    private final ClientService clientService;
//    private final ProductService productService;
    

    /** 화면 + 상태탭 포함 */
    @GetMapping
    public String listPage(
            @RequestParam(value="status", required = false) String status,
            Model model
    ) {
        model.addAttribute("status", status);
        return "sales/orders_list";
    }

    /** AG-Grid 조회 데이터 */
    @GetMapping("/list")
    @ResponseBody
    public List<OrderListDTO> list(
            @RequestParam(value ="status", required = false) String status,
            @RequestParam(value ="startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value ="endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value ="keyword",required = false) String keyword
    ) {
        return ordersService.search(status, startDate, endDate, keyword);
    }
    

  
    /* 등록 */
    @GetMapping("/new")
    public String createPage(
            Model model,
            @AuthenticationPrincipal LoginDTO login   // ← ⭐ 로그인 정보 주입
    ) {

        // 제품 목록
        List<ProductMst> products = ordersService.getProducts();      
      

        // 모델에 추가
        model.addAttribute("products", products); 
        model.addAttribute("productList", products);
        model.addAttribute("login", login);   

        return "sales/orders_create";
    }

    @GetMapping("/search-customer")
    @ResponseBody
    public List<Map<String, String>> searchCustomer(
            @RequestParam(value="keyword", required = false) String keyword
    ) {
        return ordersService.searchCustomer(keyword);
    }

    
    
    @PostMapping("/create")
    public String createOrder(
            @RequestParam(value="clientId", required = false) String clientId,
            @RequestParam(value="orderDate", required = false) String orderDate,
            @RequestParam(value="deliveryDate", required = false) String deliveryDate,
            @RequestParam(value="empId", required = false) String empId,
            @RequestParam(value="orderMemo",required = false) String orderMemo,
            @RequestParam Map<String, String> params
    ) {

    	  ordersService.createOrder(clientId, orderDate, deliveryDate, empId, orderMemo, params);
        // 저장 후 목록 페이지로 이동
        return "redirect:/sales/orders";
    }
   

}
