package com.yeoun.process.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.process.dto.ProductionDashboardKpiDTO;
import com.yeoun.process.service.ProductionDashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/production")
@RequiredArgsConstructor
public class ProductionDashboardController {
	
	private final ProductionDashboardService productionDashboardService;
	
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		
		// 상단 KPI
		ProductionDashboardKpiDTO kpi = productionDashboardService.getKpis();
        model.addAttribute("kpi", kpi);
		
		return "/process/dashboard";
	}
	

}
