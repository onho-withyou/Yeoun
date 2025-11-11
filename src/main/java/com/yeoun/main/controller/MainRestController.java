package com.yeoun.main.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yeoun.emp.dto.DeptDTO;
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
