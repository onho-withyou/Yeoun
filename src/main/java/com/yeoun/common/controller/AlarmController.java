package com.yeoun.common.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.dto.AlarmDTO;
import com.yeoun.common.service.AlarmService;

import lombok.AllArgsConstructor;


@Controller
@RequestMapping("/alarm")
@AllArgsConstructor
public class AlarmController {
	private final AlarmService alarmService;
	
	@GetMapping("")
	public String alarm() {
		return "/alarm/alarm_list";
	}
	
	@GetMapping("/list")
	public ResponseEntity<List<AlarmDTO>> getAlarmData(@AuthenticationPrincipal LoginDTO loginDTO, 
			@RequestParam(required = false, name = "startDate") String startDate,  
			@RequestParam(required = false, name = "endDate") String endDate) {
		String empId = loginDTO.getEmpId();
		
		List<AlarmDTO> alarmDTOList = alarmService.getAlarmData(empId, startDate, endDate);
		
		return ResponseEntity.ok(alarmDTOList);
	}
	
}
