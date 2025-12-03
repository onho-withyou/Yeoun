package com.yeoun.process.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.process.dto.WorkOrderProcessDTO;
import com.yeoun.process.dto.WorkOrderProcessDetailDTO;
import com.yeoun.process.service.WorkOrderProcessService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/process")
@RequiredArgsConstructor
public class WorkOrderProcessController {
	
	private final WorkOrderProcessService workOrderProcessService;
	
	// 공정 현황 페이지
	@GetMapping("/status")
	public String processStatus() {
		return "/process/process_status";
	}
	
	// 공정 현황 목록 데이터
	@GetMapping("/status/data")
	@ResponseBody
	public List<WorkOrderProcessDTO> getWorkOrdersForGrid() {
		return workOrderProcessService.getWorkOrderListForStatus();
	}
	
	// 공정 현황 상세 모달용 데이터
	@GetMapping("/status/detail/{orderId}")
	@ResponseBody
	public WorkOrderProcessDetailDTO getWorkOrderDetail(@PathVariable("orderId") String orderId) {
		return workOrderProcessService.getWorkOrderProcessDetail(orderId);
	}

}
