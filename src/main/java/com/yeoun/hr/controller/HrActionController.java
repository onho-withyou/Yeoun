package com.yeoun.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.hr.dto.HrActionRequestDTO;


@Controller
@RequestMapping("/hr")
@RequiredArgsConstructor
@Log4j2
public class HrActionController {
	
	// 인사 발령 목록 페이지 이동
	@GetMapping("/actions")
	public String getHrActionListPage() {
		return "hr/action_list";
	}
	
	// =====================================================
	// 인사 발령 등록 페이지 이동
	@GetMapping("/actions/regist")
	public String getHrActionRegistPage(Model model) {
		
		model.addAttribute("hrActionRequestDTO", new HrActionRequestDTO());
		
		// 발령구분, 부서목록, 직급목록, 결재자목록 담기
		
		return "hr/action_form";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
