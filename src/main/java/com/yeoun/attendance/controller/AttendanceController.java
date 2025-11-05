package com.yeoun.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {
	// 관리자용 출/퇴근 현황 페이지
	@GetMapping("/attendance_list")
	public String attendanceAdmin() {
		return "attendance/commute_admin";
	}
	
	// 개인 출/퇴근 현황 페이지
	@GetMapping("/my_attendance_list")
	public String attendance() {
		return "attendance/commute";
	}
	
	// 근무정책관리
	@GetMapping("/policy")
	public String policyForm() {
		return "attendance/policy";
	}
}
