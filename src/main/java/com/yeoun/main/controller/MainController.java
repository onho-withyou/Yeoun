package com.yeoun.main.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.main.dto.ScheduleDTO;
import com.yeoun.main.entity.Schedule;
import com.yeoun.main.service.ScheduleService;
import com.yeoun.notice.dto.NoticeDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainController {
	private final ScheduleService scheduleService;
	
	// 메인페이지 맵핑
	@GetMapping("")
	public String Main() {
		return "/main/main";
	}
	
	// 메인페이지 스케줄페이지
	@GetMapping("/calendar")
	public String schedule() {
		return "/main/schedule";
	}
	
	@GetMapping("/schedule")
	public String scheduleList(Model model) {
		String name = "신필용";
		model.addAttribute("name", name);
		
		return "/main/schedule_list";
	}
	
	@PostMapping("/schedule")
	public ResponseEntity<Map<String, String>> postMethodName(@ModelAttribute("scheduleDTO")@Valid ScheduleDTO scheduleDTO, BindingResult bindingResult) {
		Map<String, String> msg = new HashMap<>();
		if(bindingResult.hasErrors()) {
			msg.put("msg", "일정 등록에 실패했습니다.2222222222");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		}
		try {
			scheduleService.createSchedule(scheduleDTO);
			msg.put("msg", "일정이 등록되었습니다.");
			return ResponseEntity.ok(msg);
		
		} catch (Exception e) {
			msg.put("msg", "일정 등록에 실패했습니다 :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		}
	}
	
	
}
