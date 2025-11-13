package com.yeoun.notice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.service.NoticeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {
	private final NoticeService noticeService;
	
	// 공지사항 목록 조회
	@GetMapping("")
	public String notices(Model model,
			@RequestParam(defaultValue = "0", name = "page")int page,
		    @RequestParam(defaultValue = "10", name = "size")int size,
		    @RequestParam(defaultValue = "", name = "searchKeyword")String searchKeyword,
		    @RequestParam(defaultValue = "updatedDate", name = "orderKey")String orderKey,
		    @RequestParam(defaultValue = "", name = "orderMethod")String orderMethod) {
		
		Page<NoticeDTO> noticePage = noticeService.getNotice(page, size, searchKeyword, orderKey, orderMethod);
		
	    model.addAttribute("noticeDTOList", noticePage.getContent());
	    model.addAttribute("currentPage", noticePage.getNumber()); // 현재 페이지
	    model.addAttribute("totalPages", noticePage.getTotalPages()); // 
	    model.addAttribute("searchKeyword", searchKeyword);
	    model.addAttribute("orderKey", orderKey);
	    model.addAttribute("orderMethod", orderMethod);
	    
	    System.out.println("노티스페이지" + noticePage.getContent());
		return "/notice/notice";
	}
	
	//공지사항 등록 로직
	@PostMapping("")
	public ResponseEntity<Map<String, String>> notices(@ModelAttribute("noticeDTO") @Valid NoticeDTO noticeDTO, 
			BindingResult bindingResult, Authentication authentication) {
		Map<String, String> msg = new HashMap<>();
//		System.out.println("noticeDTO : " + noticeDTO);
		if(bindingResult.hasErrors()) {
			msg.put("msg", "공지사항 등록에 실패했습니다");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		}
		
		try {
			// 공지 등록 수행
			noticeService.createNotice(noticeDTO, authentication);
			msg.put("msg", "공지사항이 등록되었습니다.");
			return ResponseEntity.ok(msg);
		
		} catch (Exception e) {
			msg.put("msg", "공지사항 등록에 실패했습니다 :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		}
	}
	
	//공지사항 수정 로직
	@PatchMapping("/{noticeId}")
	public ResponseEntity<Map<String, String>> notices( @PathVariable("noticeId")Long noticeId,
			@ModelAttribute("noticeDTO") @Valid NoticeDTO noticeDTO, BindingResult bindingResult) {
		Map<String, String> msg = new HashMap<>();
		
		if(bindingResult.hasErrors()) {
			msg.put("msg", "공지사항 수정에 실패했습니다");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		}
		
		try {
			// 공지 수정 수행
			noticeService.modifyNotice(noticeDTO);
			msg.put("msg", "공지사항이 수정되었습니다.");
			return ResponseEntity.ok(msg);
			
		} catch (Exception e) {
			System.out.println(e);
			msg.put("msg", "공지사항 수정에 실패했습니다 :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		}
	}
	
	//공지사항 삭제 로직
	@DeleteMapping("/{noticeId}")
	public ResponseEntity<Map<String, String>> notices( @PathVariable("noticeId")Long noticeId) {
		Map<String, String> msg = new HashMap<>();
		try {
			// 공지 삭제 수행
			noticeService.deleteNotice(noticeId);
			msg.put("msg", "공지사항이 삭제되었습니다.");
			return ResponseEntity.ok(msg);
			
		} catch (Exception e) {
			System.out.println(e);
			msg.put("msg", "공지사항 삭제에 실패했습니다 :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		}
	}
}























