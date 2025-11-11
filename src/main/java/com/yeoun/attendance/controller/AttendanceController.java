package com.yeoun.attendance.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.attendance.dto.AccessLogDTO;
import com.yeoun.attendance.dto.AttendanceDTO;
import com.yeoun.attendance.dto.WorkPolicyDTO;
import com.yeoun.attendance.service.AttendanceService;
import com.yeoun.emp.dto.EmpDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Log4j2
public class AttendanceController {
	
	private final AttendanceService attendanceService;
	
	// 출/퇴근 입력
	@PostMapping("/toggle/{empId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> processAttendance(@PathVariable("empId") String empId) {
		Map<String, Object> result = new HashMap<>();
		try {
			String resultStatus = attendanceService.registAttendance(empId);
			
			result.put("success", true);
			result.put("status", resultStatus);
			
			return ResponseEntity.ok(result);
		} catch (NoSuchElementException e) {
			// 사원 정보가 없을 경우
			result.put("success", false);
			result.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
		} catch (Exception e) {
			// 그 외의 모든 예외
			result.put("success", false);
			result.put("message", e.getMessage());
			result.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}
	
	// 관리자용 출/퇴근 현황 페이지
	@GetMapping("/list")
	public String attendanceAdmin() {
		return "attendance/commute_admin";
	}
	
	// 사원번호 조회
	@GetMapping("/search")
	public ResponseEntity<?> empInfo(@RequestParam("empId") String empId) {
		try {
			EmpDTO emp = attendanceService.getEmp(empId);
//			log.info(">>>>>>>>>>>>>>>>>>> emp : " + emp);
			
			return ResponseEntity.ok(emp);
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
								 .body(Map.of("message", e.getMessage()));
		}
	}
	
	// 관리자 출/퇴근 수기 등록
	@PostMapping
	public ResponseEntity<Map<String, String>> registAttendance(@RequestBody AttendanceDTO attendanceDTO) {
		try {
			attendanceService.registAttendance(attendanceDTO);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "출퇴근 등록 완료"));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", e.getMessage()));
		}
	}
	
	// 출/퇴근 상세 내역
	@GetMapping("/{attendanceId}")
	public ResponseEntity<?> attendanceInfo(@PathVariable("attendanceId") Long attendanceId) {
		 try {
			 AttendanceDTO attendanceDTO = attendanceService.getAttendance(attendanceId);
			 return ResponseEntity.ok(attendanceDTO);
		 } catch (NoSuchElementException e) {
			 return ResponseEntity.status(HttpStatus.NOT_FOUND)
					 		      .body(Map.of("message", e.getMessage()));
		 }
	}
	
	// 출/퇴근 수정
	@PutMapping("/{attendanceId}")
	public ResponseEntity<Map<String, String>> modifyAttendance(@PathVariable("attendanceId") Long attendanceId, 
			@RequestBody AttendanceDTO attendanceDTO) {
		try {
			attendanceService.modifyAttendance(attendanceId, attendanceDTO);
			return ResponseEntity.ok(Map.of("message", "출/퇴근 수정 완료"));
		} catch (NoSuchElementException  e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", e.getMessage()));
		}
	}
	
	// 개인 출/퇴근 현황 페이지
	@GetMapping("/my")
	public String attendance() {
		return "attendance/commute";
	}
	
	// 외근 등록
	@PostMapping("/outwork")
	public String registOutwork(@ModelAttribute("accessLogDTO") AccessLogDTO accessLogDTO, RedirectAttributes redirectAttributes) {
		attendanceService.registOutwork(accessLogDTO);
		redirectAttributes.addFlashAttribute("message", "외근 등록이 완료되었습니다.");
		
		return "redirect:/attendance/my";
	}
	
	// 근무정책관리 조회
	@GetMapping("/policy")
	public String policyForm(Model model) {
		WorkPolicyDTO workPolicyDTO = attendanceService.getWorkPolicy();
		model.addAttribute("workPolicyDTO", workPolicyDTO);
		
		return "attendance/policy";
	}
	
	// 근무정책 등록
	@PostMapping("/policy")
	public String registPolicy(@ModelAttribute("workPolicyDTO") @Valid WorkPolicyDTO workPolicyDTO,  
			BindingResult bindingResult, 
			RedirectAttributes redirectAttributes) {
		
		if (bindingResult.hasErrors()) {
			return  "attendance/policy";
		}
		
		String message = attendanceService.registWorkPolicy(workPolicyDTO);
		redirectAttributes.addFlashAttribute("msg", message);
		
		return "redirect:/attendance/policy";
	}
}
