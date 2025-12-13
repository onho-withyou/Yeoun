package com.yeoun.process.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.process.dto.ProductionDashboardKpiDTO;
import com.yeoun.process.repository.WorkOrderProcessRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionDashboardService {
	
	private final WorkOrderRepository workOrderRepository;
    private final WorkOrderProcessRepository workOrderProcessRepository;

    public ProductionDashboardKpiDTO getKpis() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        long todayOrders = workOrderRepository.countTodayOrders(start, end);

        long inProgressSteps = workOrderProcessRepository.countByStatus("IN_PROGRESS");

        long qcPendingSteps = workOrderProcessRepository.countQcPendingOnly("PRC-QC");

        // 지연: 진행중 + QC대기인데 workOrder 예정완료일(planEndDate) 지남
        long delayedSteps = workOrderProcessRepository.countDelayedSteps(
                List.of("IN_PROGRESS", "QC_PENDING"), now
        );

        return ProductionDashboardKpiDTO.builder()
                .todayOrders(todayOrders)
                .inProgressSteps(inProgressSteps)
                .delayedSteps(delayedSteps)
                .qcPendingOrders(qcPendingSteps) // 필드명 qcPendingOrders지만 "QC대기 단계 수"로 써도 됨
                .qcFailSteps(0) // QC FAIL은 QC_RESULT 붙이면 여기 채우자(지금은 0 또는 임시 기준)
                .build();
    }

}
