package com.yeoun.inbound.dto;

import java.time.LocalDateTime;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class ReceiptItemDTO {
	private Integer InboundItemId; 
	private String itemName; // 제품 / 원재료 이름
	private String itemType; // 입고 타입 
	private String lotNo; // LOT 번호
	private Integer inboundAmount; // 검수 후 확정 수량
	private Integer requestAmount; // 발주 수량 / 생산 수량
	private Integer disposeAmount; // 불량 수량
	private LocalDateTime manufactureDate; // 제조일
	private LocalDateTime expirationDate;  // 유통기한
	private String locationId; // 창고위치
	private Long unitPrice; // 단가
	private Long supplyAmount; // 공급가액
	private Long totalPrice; // 합계
}
