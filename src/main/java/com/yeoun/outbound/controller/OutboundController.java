package com.yeoun.outbound.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/inventory/outbound")
@RequiredArgsConstructor
@Log4j2
public class OutboundController {
	// 출고관리 페이지
	@GetMapping("/list")
	public String outboundList(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "mat");
		return "outbound/outbound_list";
	}
	
	// 원재료 페이지
	@GetMapping("/materialList")
	public String material(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "mat");
		return "outbound/outbound_list";
	}
	
	// 완제품 페이지
	@GetMapping("/productList")
	public String product(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "pro");
		return "outbound/outbound_list";
	}
	
	
}
