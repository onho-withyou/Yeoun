package com.yeoun.process.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.process.dto.LineStayRowDTO;
import com.yeoun.process.dto.ProductionDashboardKpiDTO;
import com.yeoun.process.dto.StayCellDTO;
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
        
        // 라인
        List<LineStayRowDTO> rows = productionDashboardService.getLineStayHeatmap();
        model.addAttribute("heatmapRows", rows);

        // 전체 셀 중 진행중 건수 합
        long heatmapTotal = rows.stream()
            .flatMap(r -> r.getSteps().stream())
            .mapToLong(StayCellDTO::getInProgressCnt)
            .sum();

        model.addAttribute("heatmapTotal", heatmapTotal);
        
		return "/process/dashboard";
	}
	

}
