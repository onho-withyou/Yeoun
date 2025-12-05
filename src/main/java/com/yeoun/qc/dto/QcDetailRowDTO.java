package com.yeoun.qc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QcDetailRowDTO {
	
	// QC 결과 상세ID
	private String qcResultDtlId;
	
	// QC 항목ID
	private String qcItemId;
	
	// 항목명
	private String itemName;
	
	// 단위
	private String unit;
	
	// 기준값
	private String stdText;
	
	// 측정값
	private String measureValue;
	
	// 판정
	private String result;
	
	// 비고
	private String remark;

}
