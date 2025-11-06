package com.yeoun.emp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/emp")
public class EmpAppointmentController {
	
	@GetMapping("/appointment")
	public String appointment() {
		return "emp/appointment_list";
	}
	

}
