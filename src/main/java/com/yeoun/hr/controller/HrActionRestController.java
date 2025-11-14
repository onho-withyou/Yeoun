package com.yeoun.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.hr.dto.HrActionRequestDTO;
import com.yeoun.hr.service.HrActionService;


@Controller
@RequestMapping("/api/hr/actions")
@RequiredArgsConstructor
@Log4j2
public class HrActionRestController {
	
	private final HrActionService hrActionService;
	
	// 인사 발령 등록
	@PostMapping("")
	public Long createHrAction(@RequestBody HrActionRequestDTO dto) {
		return hrActionService.createAction(dto);
	}
	
	

}
