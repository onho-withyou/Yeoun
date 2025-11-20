package com.yeoun.approval.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.approval.dto.ApprovalDocDTO;
import com.yeoun.approval.service.ApprovalDocService;
import com.yeoun.auth.dto.LoginDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approvals")
public class ApprovalRestController {
	private final ApprovalDocService approvalDocService;
	
	// -------------------------------------------------------------------------------------------------
	// 메인페이지 결제 문서 목록 조회(내가 결제해야할문서, 내가 올린 결제문서)
	@GetMapping("")
	public ResponseEntity<List<ApprovalDocDTO>> summaryApproval(@AuthenticationPrincipal LoginDTO loginDTO) {
		String empId = loginDTO.getEmpId();
		System.out.println(empId);
		Page<ApprovalDocDTO>approvalDocDTOPage = approvalDocService.getSummaryApproval(empId);
		
		if(approvalDocDTOPage.getContent() == null) {
			return ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.ok(approvalDocDTOPage.getContent());	
	}
	
}




















