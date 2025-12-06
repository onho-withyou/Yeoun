package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.SafetyStock;
import com.yeoun.masterData.service.SafetyStockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/safetyStock")
@RequiredArgsConstructor
@Log4j2
public class SafetyStockController {
	
	private final SafetyStockService safetyStockService;
	
	//안전재고 조회
	@ResponseBody
  	@GetMapping("/list")
  	public List<SafetyStock> safetyStockList(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		return safetyStockService.findAll();
  	}

}
