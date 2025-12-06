package com.yeoun.outbound.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OutboundOrderDTO {
	private String workOrderId; // 작업지시ID
	private String shipmentId;  // 출하지시ID
	private String orderId;     // 수주ID
	private String productId;   // 제품 ID
	private String productName; // 제품 이름
	private Integer planQty;    // 수량
	private String createdId;   // 작성자Id(출고요청자)
	private LocalDateTime startDate; // 출고예정일
	private List<OutboundOrderItemDTO> items; // 출고품목
}
