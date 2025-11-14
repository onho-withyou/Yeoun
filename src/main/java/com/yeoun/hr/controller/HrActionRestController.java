package com.yeoun.hr.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.service.EmpService;
import com.yeoun.hr.dto.HrActionRequestDTO;
import com.yeoun.hr.service.HrActionService;


@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@Log4j2
public class HrActionRestController {
	
	private final HrActionService hrActionService;
	private final EmpService empService;
	
	// 인사 발령 등록
	@PostMapping("/actions")
	public Long createHrAction(@RequestBody HrActionRequestDTO dto) {
		return hrActionService.createAction(dto);
	}
	
	// 발령 화면용 사원 목록
	@GetMapping("/employees")
	public List<EmpListDTO> getEmployeesForAction() {
		return empService.getEmpList();
	}
	
	

}
