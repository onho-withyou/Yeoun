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
public class WorkOrderDTO {
	private String orderId;					// 작업지시 ID
	private String planId;					// 생산계획 ID
	private String productId;				// 제품 ID
	private String productName;				// 제품 이름
    private Integer planQty;				// 계획수량
    private LocalDateTime planStartDate;	// 예정시작일시
    private LocalDateTime ActStartDate;		// 실제시작일시
    private LocalDateTime planEndDate;		// 예정완료일시
    private LocalDateTime ActEndDate;		// 실제완료일시
    private String lineId;					// 수행 라인
    private String status;					// 수행 상태
    private String createdId;				// 작성자 ID
    private LocalDateTime createdDate;		// 작성일자
    private LocalDateTime updatedDate;		// 수정일자
    private String remark;					// 비고(특이사항 및 메모)
}
