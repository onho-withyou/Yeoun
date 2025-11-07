package com.yeoun.attendance.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.attendance.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
	
	private final AttendanceService attendanceService;
	
	// 출/퇴근 입력
	@PostMapping("/toggle/{empId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> processAttendance(@PathVariable("empId") String empId) {
		try {
			String resultStatus = attendanceService.registAttendance(empId);
			
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("success", true);
			res.put("status", resultStatus);
			
			return ResponseEntity.ok(res);
		} catch (NoSuchElementException e) {
			// 사원 정보가 없을 경우
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} catch (Exception e) {
			// 그 외의 모든 예외
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	
	
	
	// 관리자용 출/퇴근 현황 페이지
	@GetMapping("/list")
	public String attendanceAdmin() {
		return "attendance/commute_admin";
	}
	
	// 개인 출/퇴근 현황 페이지
	@GetMapping("/my")
	public String attendance() {
		return "attendance/commute";
	}
	
	// 근무정책관리
	@GetMapping("/policy")
	public String policyForm() {
		return "attendance/policy";
	}
}
