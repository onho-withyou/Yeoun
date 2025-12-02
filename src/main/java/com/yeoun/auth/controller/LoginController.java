package com.yeoun.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
	
	// 로그인 폼 포워딩 시 아이디 기억하기 쿠키 사용을 위해
	// 메서드 파라미터로 @CookieValue 어노테이션 활용하여 변수 선언
	@GetMapping("/login")
	public String login(@CookieValue(value = "remember-id", required = false) String rememberId, Model model) {
		
		// 쿠키값 Model 객체에 추가
		model.addAttribute("rememberId", rememberId);
		
		return "/login/login";
	}
	

}
