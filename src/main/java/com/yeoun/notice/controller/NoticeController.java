package com.yeoun.notice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/notice")
public class NoticeController {
	@GetMapping("")
	public String notice() {
		return "/notice/notice";
	}
	
}
