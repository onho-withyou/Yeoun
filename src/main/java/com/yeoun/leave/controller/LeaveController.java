package com.yeoun.leave.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.attendance.controller.AttendanceController;
import com.yeoun.attendance.service.AttendanceService;
import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.service.CommonCodeService;
import com.yeoun.leave.dto.LeaveChangeRequestDTO;
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
		LeaveDTO leaveDTO = leaveService.getAnnualLeave(loginDTO.getEmpId());
		
		leaveService.createAnnualLeave(202511150104L);
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
		
		return ResponseEntity.ok(leaveList);
	}
	
	// 연차 조회(개별)
	@GetMapping("/{leaveId}")
	@ResponseBody
	public ResponseEntity<LeaveDTO> leaveDetailInfo(@PathVariable("leaveId") Long leaveId) {
		
		LeaveDTO leaveDTO = leaveService.getLeaveDetail(leaveId);
		
		return ResponseEntity.ok(leaveDTO);
	}
	
	// 연차 수정
	@PostMapping("/{leaveId}")
	public ResponseEntity<Map<String, String>> modifyLeave(
			@PathVariable("leaveId") Long leaveId, 
			@AuthenticationPrincipal LoginDTO loginDTO,
			@RequestBody LeaveChangeRequestDTO leaveChangeRequestDTO) {
		
		try {
			leaveService.modifyLeave(loginDTO, leaveChangeRequestDTO, leaveId);
			return ResponseEntity.ok(Map.of("message", "연차 수정 완료"));
		} catch (Exception e) {
			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("message", "연차 수정 시 오류가 발생했습니다."));
		}
	}
}
