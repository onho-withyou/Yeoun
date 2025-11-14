package com.yeoun.hr.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.emp.service.EmpService;
import com.yeoun.hr.dto.HrActionRequestDTO;


@Controller
@RequestMapping("/hr")
@RequiredArgsConstructor
@Log4j2
public class HrActionController {
	
	private final EmpService empService;
	private final DeptRepository deptRepository;
	
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
		
		List<Dept> allDepts = deptRepository.findActive();
		
		// 상위부서 제외하고 하위 부서만 남김
		List<Dept> deptList = allDepts.stream()
								.filter(d -> d.getParentDeptId() != null)
								.toList();
		
		model.addAttribute("deptList", deptList);
		model.addAttribute("posList", empService.getPositionList());
		
		return "hr/action_form";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
