package com.yeoun.qc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/qc")
public class QcController {
	
	// QC 등록 페이지
	@GetMapping("/regist")
	public String qcRegistList() {
		return "/qc/regist_list";
	}
	
	// QC 결과 조회 페이지
	@GetMapping("/result")
	public String qcResultList() {
		return "/qc/result_list";
	}
	

}
