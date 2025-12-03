package com.yeoun.order.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
// 작업지시 목록 조회 DTO
public class WorkOrderListDTO {
	
	private String orderId;			// 작업지시 번호
	private String productId;		// 품번
	private String itemName;		// 품목군
	private Integer planQty;		// 계획 수량
	private LocalDateTime startTime;// 예정 시작 시간
	private LocalDateTime endTime;	// 예정 종료 시간
	private String status;			// 상태
	private LocalDateTime createdTime;	// 등록 시간 (최근 등록 순 정렬)

}
