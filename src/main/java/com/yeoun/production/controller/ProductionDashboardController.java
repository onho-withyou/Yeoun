package com.yeoun.production.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/production")
public class ProductionDashboardController {
	
	@GetMapping("/dashboard")
	public String dashboard() {
		return "/production/dashboard";
	}
	

}
