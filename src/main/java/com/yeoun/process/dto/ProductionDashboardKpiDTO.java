package com.yeoun.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionDashboardKpiDTO {
	
	private long todayOrders;		// 오늘 작업지시 (생성)
	private long inProgressSteps;	// 진행 중 공정 (단계)
	private long delayedSteps;		// 지연 공정 (단계)
	private long qcPendingOrders;	// QC 대기
	private long qcFailSteps;		// QC 불합격
	

}
