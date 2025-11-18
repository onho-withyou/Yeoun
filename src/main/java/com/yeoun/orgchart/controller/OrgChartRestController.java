package com.yeoun.orgchart.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.orgchart.dto.OrgNodeDTO;
import com.yeoun.orgchart.service.OrgChartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrgChartRestController {
	
	private final OrgChartService orgChartService;
	
	@GetMapping("/tree")
	public List<OrgNodeDTO> getOrgTree() {
		return orgChartService.getOrgTree();
	}
	

}
