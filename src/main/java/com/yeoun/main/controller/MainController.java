package com.yeoun.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/main")
public class MainController {
	@GetMapping("")
	public String Main() {
		return "/main/main";
	}
	
}
