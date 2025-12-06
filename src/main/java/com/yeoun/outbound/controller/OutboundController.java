package com.yeoun.outbound.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.outbound.dto.OutboundOrderDTO;
import com.yeoun.outbound.service.OutboundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/inventory/outbound")
@RequiredArgsConstructor
@Log4j2
public class OutboundController {
	private final OutboundService outboundService;
	
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
	
	// 출고 등록(원재료)
	@PostMapping("/mat/regist")
	public ResponseEntity<Map<String, String>> registMatOutbound(@RequestBody OutboundOrderDTO outboundOrderDTO, @AuthenticationPrincipal LoginDTO loginDTO) {
		try {
			outboundService.saveOutbound(outboundOrderDTO, loginDTO.getEmpId());
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "등록 완료"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", e.getMessage()));
		}
	}
	
	
}
