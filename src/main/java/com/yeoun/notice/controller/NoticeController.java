package com.yeoun.notice.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
	private final NoticeService noticeService;
	
	@GetMapping("")
	public String notice(Model model) {
		
		List<NoticeDTO> noticeList = noticeService.getNotice();
		
		return "/notice/notice";
	}
	
}
