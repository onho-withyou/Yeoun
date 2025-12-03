package com.yeoun.masterData.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.auth.dto.LoginDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/masterData")
@RequiredArgsConstructor
@Log4j2
public class QcItemController {

    //품질항목관리 연결페이지(검사 X)
  	@GetMapping("/qc_item")
  	public String qcItem(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		//model.addAttribute("empList", approvalDocService.getEmp());//기안자 목록 불러오기
		return "masterData/qc_item";
 	}
    
}
