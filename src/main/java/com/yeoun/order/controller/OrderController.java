package com.yeoun.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.order.dto.WorkOrderListDTO;
import com.yeoun.order.service.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
@Log4j2
public class OrderController {
	
	private final OrderService orderService;

    @GetMapping("/list")
    public String list (WorkOrderListDTO dto){
    	List<WorkOrderListDTO> dtoList = orderService.loadAllOrders(dto);
    	log.info("list 페이지 진입......... dtoList :::::: " + dtoList);
        return "/order/list";
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
