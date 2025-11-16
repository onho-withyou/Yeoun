package com.yeoun.leave.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.attendance.controller.AttendanceController;
import com.yeoun.attendance.service.AttendanceService;
import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.service.CommonCodeService;
import com.yeoun.leave.dto.LeaveDTO;
import com.yeoun.leave.dto.LeaveHistoryDTO;
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
	
	// 개인 연차에 대한 간단한 정보
	@GetMapping("/my")
	public String leave(@AuthenticationPrincipal LoginDTO loginDTO, Model model) {
//		leaveService.createAnnualLeaveForEmp(loginDTO.getEmpId());
		LeaveDTO leaveDTO = leaveService.getAnnualLeave(loginDTO.getEmpId());
		
		model.addAttribute("leaveDTO", leaveDTO);
		
		return "leave/leave";
	}
	
	// 개인 연차 현황 (리스트)
	@GetMapping("/my/data")
	@ResponseBody
	public ResponseEntity<List<LeaveHistoryDTO>> leaveList(
			@AuthenticationPrincipal LoginDTO loginDTO,
			@RequestParam(required = false, name = "startDate") String startDate,  
			@RequestParam(required = false, name = "endDate") String endDate) {
		
		LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
		LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);
		
		List<LeaveHistoryDTO> leaveList = leaveService.getMyLeaveList(loginDTO.getEmpId(), startOfYear, endOfYear);
		
		return ResponseEntity.ok(leaveList);
	}
	
	// 관리자용 연차 현황 (리스트)
	@GetMapping("/list/data")
	@ResponseBody
	public ResponseEntity<List<LeaveDTO>> leaveListAllEmp(@RequestParam(required = false, name = "empId") String empId) {
		
		List<LeaveDTO> leaveList = leaveService.getAllLeaveList(empId);
		
		log.info(">>>>>>>>>>>>> empId : " + empId);
		
		return ResponseEntity.ok(leaveList);
	}
}
