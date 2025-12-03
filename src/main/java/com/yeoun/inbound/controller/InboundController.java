package com.yeoun.inbound.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory/inbound")
public class InboundController {

	@GetMapping("/list")
	public String inboundList(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "mat");
		return "inbound/inbound_list";
	}
	
	@GetMapping("/materialList")
	public String materialList(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "mat");
		return "inbound/inbound_list";
	}
	
	@GetMapping("/productList")
	public String productList(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "pro");
		return "inbound/inbound_list";
	}
}
