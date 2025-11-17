package com.yeoun.orgchart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orgchart")
public class OrgChartController {
	
	@GetMapping("")
	public String orgchartPage() {
		return "orgchart/orgchart";
	}
	

}

