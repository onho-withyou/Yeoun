package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.MaterialMst;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.service.MaterialMstService;
import com.yeoun.masterData.service.ProductMstService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/material")
@RequiredArgsConstructor
@Log4j2
public class MaterialMstController {
	
	private final MaterialMstService materialMstService;
	
	//원재료 조회
	@ResponseBody
  	@GetMapping("/list")
  	public List<MaterialMst> materialList(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		return materialMstService.findAll();
  	}

}
