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
    	model.addAttribute("plans", orderService.loadAllPlans());		// 생산계획 조회 
    	model.addAttribute("prods", orderService.loadAllProducts());	// 품목 조회
    	model.addAttribute("lines", orderService.loadAllLines());		// 라인 조회
        model.addAttribute("leadWorkers", 
        						orderService.loadAllWorkers("POS002"));	// 작업자 조회(작업반장)
        model.addAttribute("workers", 
        						orderService.loadAllWorkers("POS001"));	// 작업자 조회(작업자)
        
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
