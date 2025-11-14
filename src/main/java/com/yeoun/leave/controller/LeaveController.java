package com.yeoun.leave.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.attendance.controller.AttendanceController;
import com.yeoun.attendance.service.AttendanceService;
import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.service.CommonCodeService;
import com.yeoun.leave.dto.LeaveDTO;
import com.yeoun.leave.service.LeaveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/leave")
@RequiredArgsConstructor
@Log4j2
public class LeaveController {
	
	private final LeaveService leaveService;
	
	// 관리자용 연차 현황
	@GetMapping("/list")
	public String leaveAdmin() {
		return "leave/leave_admin";
	}
	
	// 개인 연차 현황
	@GetMapping("/my")
	public String leave(@AuthenticationPrincipal LoginDTO loginDTO, Model model) {
//		leaveService.createAnnualLeaveForEmp(loginDTO.getEmpId());
		LeaveDTO leaveDTO = leaveService.getAnnualLeave(loginDTO.getEmpId());
		
		model.addAttribute("leaveDTO", leaveDTO);
		
		return "leave/leave";
	}
}
