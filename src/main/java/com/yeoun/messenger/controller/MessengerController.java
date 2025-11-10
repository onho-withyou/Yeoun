package com.yeoun.messenger.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/messenger")
@Log4j2
public class MessengerController {
	
	@GetMapping(value = {"/", "/index"})
	public String index(Model model) {
		return "/messenger/index";
	}
	
	@GetMapping("/popup")
	public String popup(Model model) {
		return "/messenger/popup";
	}
	
	@GetMapping("/chat")
	public String chat(Model model) {
		return "/messenger/chat";
	}
	

}
