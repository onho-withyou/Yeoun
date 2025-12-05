package com.yeoun.qc.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.qc.dto.QcDetailRowDTO;
import com.yeoun.qc.dto.QcRegistDTO;
import com.yeoun.qc.service.QcResultService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/qc")
@RequiredArgsConstructor
public class QcController {
	
	private final QcResultService qcResultService;
	
	// QC 등록 페이지
	@GetMapping("/regist")
	public String qcRegistList() {
		return "/qc/regist_list";
	}
	
	// QC 등록 목록 데이터
	@GetMapping("/regist/data")
	@ResponseBody
	public List<QcRegistDTO> qcRegistListForGrid() {
		return qcResultService.getQcResultListForRegist();
	}
	
	// QC 등록 모달 데이터
	@GetMapping("/{qcResultId}/details")
	@ResponseBody
	public List<QcDetailRowDTO> qcRegistDetails(@PathVariable("qcResultId") Long qcResultId) {
		return qcResultService.getDetailRows(qcResultId);
	}
	

	
	// QC 결과 조회 페이지
	@GetMapping("/result")
	public String qcResultList() {
		return "/qc/result_list";
	}
	

}
