package com.yeoun.notice.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
	private final NoticeService noticeService;
	
	@GetMapping("")
	public String notice(Model model,
			@RequestParam(defaultValue = "0", name = "page")int page,
		    @RequestParam(defaultValue = "10", name = "size")int size) {
		
		List<NoticeDTO> noticeDTOList = noticeService.getNotice(page, size);
		
		System.out.println("11111111111" + noticeDTOList);
		
		for(NoticeDTO notice : noticeDTOList) {
			System.out.println(notice.getNoticeId());
		}
		
		model.addAttribute("noticeDTOList", noticeDTOList);
		
		return "/notice/notice";
	}
	
}
