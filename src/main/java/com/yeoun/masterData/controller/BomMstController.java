package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.BomMst;
import com.yeoun.masterData.service.BomMstService;
import com.yeoun.outbound.dto.OutboundOrderItemDTO;

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
 	// 1. DB 조회는 한 번만 수행
 	    List<BomMst> bomList = bomMstService.findAll();
 	    
 	    // 2. 로그를 찍을 때 변수를 사용
 	    log.info("bomMstService.findAll()-------------> 조회된 개수: {}", bomList.size());
 	    
 	    // 3. 변수를 반환
 	    return bomList;
  	}
 	
 	// prdId에 해당하는 BOM 리스트 조회
 	@GetMapping("/list/data/{prdId}")
 	@ResponseBody
 	public ResponseEntity<List<OutboundOrderItemDTO>> outboundBomList(@PathVariable("prdId") String prdId) {
 		
 		List<OutboundOrderItemDTO> bomList = bomMstService.getBomListByPrdId(prdId);
 		
 		return ResponseEntity.ok(bomList);
 	}

}
