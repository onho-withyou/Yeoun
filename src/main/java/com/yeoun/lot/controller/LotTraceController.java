package com.yeoun.lot.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.lot.dto.LotMaterialNodeDTO;
import com.yeoun.lot.dto.LotProcessNodeDTO;
import com.yeoun.lot.dto.LotRootDTO;
import com.yeoun.lot.entity.LotMaster;
import com.yeoun.lot.service.LotTraceService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/lot")
@RequiredArgsConstructor
public class LotTraceController {

	private final LotTraceService lotTraceService;
	
	// LOT 추적 페이지
	@GetMapping("/trace")
	public String view(@RequestParam(name = "lotNo", required = false) String lotNo, 
					   @RequestParam(name = "stepSeq", required = false) Integer stepSeq,
					   Model model) {
		
		// 1) 왼쪽에 표시할 WIP + FIN LOT 목록
		List<LotRootDTO> lotList = lotTraceService.getFinishedLots(); 
		model.addAttribute("lotList", lotList);
		
		return "/lot/trace";
	}
	
	// LOT 상세 + 공정 + 자재 LOT를 한 번에 가져와서
	// 오른쪽 카드(body) fragment 로만 렌더링
	@GetMapping("/trace/detail")
	public String getLotDetailFragment(@RequestParam("lotNo") String lotNo,
	                                   @RequestParam(name = "stepSeq", required = false) Integer stepSeq,
	                                   Model model) {

	    // LOT 기본 정보
	    LotMaster selected = lotTraceService.getLotDetail(lotNo);
	    model.addAttribute("selectedLot", selected);

	    // 공정 단계 목록
	    List<LotProcessNodeDTO> processNodes = lotTraceService.getProcessNodesForLot(lotNo);
	    model.addAttribute("processNodes", processNodes);

	    // 선택된 공정 단계 (있으면)
	    if (stepSeq != null) {
	        processNodes.stream()
	                .filter(p -> p.getStepSeq().equals(stepSeq))
	                .findFirst()
	                .ifPresent(p -> model.addAttribute("selectedProcess", p));
	    }

	    // 자재 LOT 목록
	    List<LotMaterialNodeDTO> materialNodes = lotTraceService.getMaterialNodesForLot(lotNo);
	    model.addAttribute("materialNodes", materialNodes);

	    // 오른쪽 상세 카드용 fragment만 반환
	    return "/lot/trace_detail :: detail";
	}
	
	
	
	
	
	
}
