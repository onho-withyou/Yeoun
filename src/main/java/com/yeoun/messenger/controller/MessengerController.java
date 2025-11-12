package com.yeoun.messenger.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.messenger.dto.MsgStatusDTO;
import com.yeoun.messenger.service.MessengerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/messenger")
@RequiredArgsConstructor
@Log4j2
public class MessengerController {
	
	private final MessengerService messengerService;
	
	// ==========================================================================
	// 메신저 큰 화면 (보류)
	@GetMapping(value = {"/", "/index"})
	public String index(Model model) {
		return "/messenger/index";
	}
	
	// ==========================================================================
	// 메신저 팝업 목록 - 친구목록 / 대화목록
	@GetMapping("/list")
	public String list(Authentication authentication, Model model) {
		LoginDTO loginDTO = (LoginDTO)authentication.getPrincipal();
		List<MsgStatusDTO> msgStatusDTOList = messengerService.selectUsers(loginDTO.getUsername());
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> username : " + loginDTO.getUsername());
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> list : " + msgStatusDTOList);
		model.addAttribute("friends", msgStatusDTOList);
		return "/messenger/list";
	}
	
	// ==========================================================================
	// 메신저 팝업 채팅방
	@GetMapping("/chat")
	public String chat(Model model) {
		return "/messenger/chat";
	}
	

}
