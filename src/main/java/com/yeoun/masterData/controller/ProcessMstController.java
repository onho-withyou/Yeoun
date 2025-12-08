package com.yeoun.masterData.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.service.ProcessMstService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/masterData")
@RequiredArgsConstructor
@Log4j2
public class ProcessMstController {
	
	@Autowired
	private ProcessMstService processMstService;
	

    //공정(라우트/코드)관리 연결페이지
  	@GetMapping("/process_mst")
  	public String processMst(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
  		model.addAttribute("prdMstList", processMstService.getPrdMst());//라우트 제품코드 불러오기
		return "masterData/process_mst";
 	}
  	
  	
  	//제품별 공정라우트 조회
  	@GetMapping("/process/list")
  	public List<ProcessMst> processList(Model model, @AuthenticationPrincipal LoginDTO loginDTO,@RequestBody Map<String,Object> searchParams) {
		return processMstService.findAll(searchParams);
  	}
    
}
