package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.BomMst;
import com.yeoun.masterData.service.BomMstService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/bom")
@RequiredArgsConstructor
@Log4j2
public class BomMstController {
	
	private final BomMstService bomMstService; 
	
 	@ResponseBody
  	@GetMapping("/list")
  	public List<BomMst> bomList(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
 	    List<BomMst> bomList = bomMstService.findAll();
 	    
 	    log.info("bomMstService.findAll()-------------> 조회된 개수: {}", bomList.size());
 	    
 	    return bomList;
  	}

}
