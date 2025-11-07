package com.yeoun.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/main")
public class MainController {
	// 메인페이지 맵핑
	@GetMapping("")
	public String Main() {
		return "/main/main";
	}
	
	// 메인페이지 스케줄페이지
	@GetMapping("/calendar")
	public String schedule() {
		return "/main/schedule";
	}
	
	@GetMapping("/schedule")
	public String scheduleList(Model model) {
		String name = "신필용";
		model.addAttribute("name", name);
		
		return "/main/scheduleList";
	}
	
}
