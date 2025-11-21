package com.yeoun.main.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.emp.dto.DeptDTO;
import com.yeoun.leave.dto.LeaveDTO;
import com.yeoun.leave.dto.LeaveHistoryDTO;
import com.yeoun.main.dto.ScheduleDTO;
import com.yeoun.main.dto.ScheduleSharerDTO;
import com.yeoun.main.service.ScheduleService;
import com.yeoun.main.service.ScheduleSharerService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class MainRestController {
	private final ScheduleService scheduleService;
	private final ScheduleSharerService scheduleSharerService; 
	
	// 일정목록 조회
	@GetMapping("")
	@ResponseBody
	public ResponseEntity<List<ScheduleDTO>> schedules(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate
			, @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate
			, Authentication authentication, Principal principal) {
		List<ScheduleDTO> scheduleDTOList = scheduleService.getScheduleList(startDate, endDate, authentication);
		
//		return ResponseEntity.ok(null);
		return ResponseEntity.ok(scheduleDTOList);
	}
	
	// 단일 일정 조회
	@GetMapping("/{scheduleId}")
	@ResponseBody
	public ResponseEntity<ScheduleDTO> schedules(@PathVariable("scheduleId")String scheduleId) {
		Long scheduleIdl = Long.parseLong(scheduleId); 
		ScheduleDTO scheduleDTO = scheduleService.getSchedule(scheduleIdl);
		
		return ResponseEntity.ok(scheduleDTO);
	}
	
	
	// 일정등록 부서목록 조회
	@GetMapping("/departments")
	@ResponseBody
	public ResponseEntity<List<DeptDTO>> departments() {
        List<DeptDTO> deptList = scheduleService.getDeptList();
        
        if (deptList == null) { //찾는 공지사항이 없을때
            return ResponseEntity.notFound().build();
        }
        
		return ResponseEntity.ok(deptList);
	}
	
	//연차 정보 조회
	@GetMapping("/leaves")
	@ResponseBody
	public ResponseEntity<List<LeaveHistoryDTO>> leaves(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate
			, @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate
			, Authentication authentication, Principal principal) {
		LoginDTO loginDTO = (LoginDTO)authentication.getPrincipal();
		List<LeaveHistoryDTO> leaveHistoryList = scheduleService.getLeaveHistoryList(startDate, endDate, loginDTO);
//		return ResponseEntity.ok(null);
//		System.out.println(leaveHistoryList);
		return ResponseEntity.ok(leaveHistoryList);
	}
	
	// 부서정보 불러오기
	@GetMapping("/organizationChart")
	public ResponseEntity<Map<String, Object>> getOrganizationChart() {
		
		List<Map<String, Object>> organizationList = scheduleService.getOrganizationList();
		
		Map<String, Object> result = new HashMap<>();
		result.put("data", organizationList);
		result.put("count", organizationList.size());
		
		return ResponseEntity.ok(result);
	}
	
	// 공유일정 공유자목록 불러오기
	@GetMapping("/sharerList/{scheduleId}")
	@ResponseBody
	public ResponseEntity<List<ScheduleSharerDTO>> getSharerList(@PathVariable("scheduleId")String scheduleId) {
		Long scheduleIdl = Long.parseLong(scheduleId); 
		List<ScheduleSharerDTO> sharerDTOList = scheduleSharerService.getSchedule(scheduleIdl);
		
		return ResponseEntity.ok(sharerDTOList);
	}
	
}
