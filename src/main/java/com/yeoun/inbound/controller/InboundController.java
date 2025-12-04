package com.yeoun.inbound.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.inbound.dto.InboundDTO;
import com.yeoun.inbound.dto.ReceiptDTO;
import com.yeoun.inbound.service.InboundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/inventory/inbound")
@RequiredArgsConstructor
@Log4j2
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
	@GetMapping("/materialList/data")
	@ResponseBody
	public ResponseEntity<List<ReceiptDTO>> materialList(
			@RequestParam(required = false, name = "startDate") String startDate, 
			@RequestParam(required = false, name = "endDate") String endDate,
			@RequestParam(required = false, name = "searchType") String searchType,
			@RequestParam(required = false, name = "keyword") String keyword) {
		
		// 문자열로 들어온 날짜 데이터를 변환
		LocalDateTime start = (startDate != null) ? LocalDate.parse(startDate).atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
		LocalDateTime end = (endDate != null) ? LocalDate.parse(endDate).atTime(23, 59, 59) : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
		
		List<ReceiptDTO> inboundList = inboundService.getMaterialInboundList(start, end, searchType, keyword);
		
		return ResponseEntity.ok(inboundList);
	}
	
	// 원재료 입고 상세페이지
	@GetMapping("/mat/{inboundId}")
	public String materialDetail(@PathVariable("inboundId") String inboundId, Model model) {
		
		ReceiptDTO receiptDTO = inboundService.getMaterialInbound(inboundId);
		
		return "inbound/inbound_detail";
	}
}
