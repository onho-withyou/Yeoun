package com.yeoun.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/hr")
@RequiredArgsConstructor
@Log4j2
public class HrActionController {
	
	// 인사 발령 목록
	@GetMapping("/list")
	public String appointment() {
		return "hr/action_list";
	}

}
