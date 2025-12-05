package com.yeoun.order.controller;

import com.yeoun.order.dto.WorkOrderSearchDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.order.dto.WorkOrderListDTO;
import com.yeoun.order.service.OrderService;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
@Log4j2
public class OrderController {
	
	private final OrderService orderService;

    // ======================================================
    // 작업지시 목록
    @GetMapping("/list")
    public String list (WorkOrderListDTO dto, Model model){
        return "/order/list";
    }

    // =====================================================
    // 작업지시 목록 조회
    @GetMapping("/list/data")
    @ResponseBody
    public List<WorkOrderListDTO> listData (WorkOrderSearchDTO dto){
        return orderService.loadAllOrders(dto);
    }

    @GetMapping("/schedule")
    public String schedule (){
        return "/order/schedule";
    }

    @GetMapping("/line")
    public String line (){
        return "/order/line";
    }
}
