package com.yeoun.lot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/lot")
public class LotTraceController {
	
	// LOT 추적 페이지
	@GetMapping("/trace")
	public String lotTrace() {
		return "/lot/trace";
	}
	

}
