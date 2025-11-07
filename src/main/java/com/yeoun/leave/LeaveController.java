package com.yeoun.leave;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/leave")
public class LeaveController {
	// 관리자용 연차 현황
	@GetMapping("/list")
	public String leaveAdmin() {
		return "leave/leave_admin";
	}
	
	// 개인 연차 현황
	@GetMapping("/my")
	public String leave() {
		return "leave/leave";
	}
}
