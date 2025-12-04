package com.yeoun.qc.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QcRegistDTO {
	
	// 작업지시번호
	private String orderId;
	
	// 제품코드
	private String prdId;
	
	// 제품명
	private String prdName;
	
	// 계획수량
	private Integer planQty;
	
	// QC 상태
	private String overallResult;
	
	// 검사일
	private LocalDate inspectionDate;

}
