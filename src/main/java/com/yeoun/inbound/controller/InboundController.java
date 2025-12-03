package com.yeoun.inbound.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.inbound.dto.InboundDTO;
import com.yeoun.inbound.service.InboundService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/inventory/inbound")
@RequiredArgsConstructor
public class InboundController {
	private final InboundService inboundService;

	// 입고관리 페이지
	@GetMapping("/list")
	public String inboundList(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "mat");
		return "inbound/inbound_list";
	}
	
	// 원재료 페이지
	@GetMapping("/materialList")
	public String material(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "mat");
		return "inbound/inbound_list";
	}
	
	// 완제품 페이지
	@GetMapping("/productList")
	public String product(Model model) {
		// 탭 활성화를 위한 정보
		model.addAttribute("activeTab", "pro");
		return "inbound/inbound_list";
	}
	
	// 원재료 목록 데이터(날짜 지정과 검색 기능 포함)
	@GetMapping("/inventory/inbound/materialList/data")
	@ResponseBody
	public ResponseEntity<List<InboundDTO>> materialList(
			@RequestParam(required = false, name = "startDate") String startDate, 
			@RequestParam(required = false, name = "endDate") String endDate,
			@RequestParam(required = false, name = "keyword") String keyword) {
		
		// 문자열로 들어온 날짜 데이터를 변환
		LocalDateTime start = (startDate != null) ? LocalDate.parse(startDate).atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
		LocalDateTime end = (endDate != null) ? LocalDate.parse(endDate).atTime(23, 59, 59) : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
		
		List<InboundDTO> inboundList = inboundService.getMaterialInboundList(startDate, endDate, keyword);
		
		return ResponseEntity.ok(inboundList);
	}
}
