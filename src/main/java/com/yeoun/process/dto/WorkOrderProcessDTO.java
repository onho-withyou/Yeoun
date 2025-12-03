package com.yeoun.process.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderProcessDTO {
	
	// 작업지시번호
	private String orderId;
	
	// 제품코드
	private String prdId;
	
	// 제품명
	private String prdName;
	
	// 계획수량
	private Integer planQty;
	
	// 양품수량
	// 진행률
	// 현재공정
	// 상태
	private String status;
	
	// 시간

}
