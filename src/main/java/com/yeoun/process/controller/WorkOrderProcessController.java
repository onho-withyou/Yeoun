package com.yeoun.process.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/process")
public class WorkOrderProcessController {
	
	// 공정 현황 페이지
	@GetMapping("/status")
	public String processStatus() {
		return "/process/process_status";
	}
	

}
