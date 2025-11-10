package com.yeoun.main.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yeoun.emp.dto.DeptDTO;
import com.yeoun.main.dto.ScheduleDTO;
import com.yeoun.main.service.ScheduleService;
import com.yeoun.notice.dto.NoticeDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class MainRestController {
	private final ScheduleService scheduleService;
	
	// 일정목록 조회
	@GetMapping("")
	@ResponseBody
	public ResponseEntity<List<ScheduleDTO>> schedules(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		
		List<ScheduleDTO> scheduleDTOList = scheduleService.getScheduleList(startDate, endDate);
		
		System.out.println(scheduleDTOList);
		return ResponseEntity.ok(scheduleDTOList);
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
	
}
