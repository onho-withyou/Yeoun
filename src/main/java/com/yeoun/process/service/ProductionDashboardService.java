package com.yeoun.process.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.process.dto.ProductionDashboardKpiDTO;
import com.yeoun.process.repository.WorkOrderProcessRepository;
import com.yeoun.qc.repository.QcResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionDashboardService {
	
	private final QcResultRepository qcResultRepository;
	private final WorkOrderRepository workOrderRepository;
    private final WorkOrderProcessRepository workOrderProcessRepository;

    public ProductionDashboardKpiDTO getKpis() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        LocalDate tomorrow = today.plusDays(1);
        
        // 큰 숫자
        // 오늘 작업지시
        long todayOrders = workOrderRepository.countTodayOrders(start, end);
        // 진행 중 작업지시 수
        long inProgressOrders = workOrderRepository.countByStatus("IN_PROGRESS");
        // 지연 작업지시 수 (예정완료시간 초과)
        long delayedOrders = workOrderRepository.countByStatusAndPlanEndDateBefore("IN_PROGRESS", now);
        // QC 대기 단계 수
        long qcPendingSteps = workOrderProcessRepository.countQcPendingOnly("PRC-QC");
        // QC 불합격 수
        long qcFailSteps = 
        		qcResultRepository.countByOverallResultAndInspectionDateGreaterThanEqualAndInspectionDateLessThan(
        				"FAIL", today, tomorrow
        	    );
        
        // 작은 숫자
        // 오늘 완료된 작업지시
        long completedToday = workOrderRepository.countByStatusAndActEndDateBetween("COMPLETED", start, end);
        // 오늘 기준 대기(진행중)
        long waitingToday = workOrderRepository.countByStatus("IN_PROGRESS");
        return ProductionDashboardKpiDTO.builder()
                .todayOrders(todayOrders)
                .inProgressOrders(inProgressOrders)
                .delayedOrders(delayedOrders)
                .qcPendingOrders(qcPendingSteps) 
                .qcFailSteps(qcFailSteps) 
                .completedDelta(completedToday)
                .waitingDelta(waitingToday)
                .build();
    }

}
