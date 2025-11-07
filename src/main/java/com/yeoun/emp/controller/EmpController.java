package com.yeoun.emp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/emp")
public class EmpController {
	
	@GetMapping("/list")
	public String List() {
		return "emp/emp_list";
	}
	
	@GetMapping("/register")
	public String registerEmployee() {
		return "emp/emp_register";
	}
	

}
