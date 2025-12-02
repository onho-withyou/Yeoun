package com.yeoun.sales.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.sales.dto.OrderListDTO;
import com.yeoun.sales.service.OrdersService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales/orders")
public class OrdersController {

    private final OrdersService ordersService;

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
}
