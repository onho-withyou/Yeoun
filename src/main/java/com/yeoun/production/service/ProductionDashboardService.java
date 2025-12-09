package com.yeoun.production.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.process.dto.WorkOrderProcessDTO;
import com.yeoun.process.service.WorkOrderProcessService;
import com.yeoun.production.dto.ProductionDashboardDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionDashboardService {
	
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderProcessService workOrderProcessService; // ⭐ 기존 공정현황 서비스 사용

    @Transactional(readOnly = true)
    public ProductionDashboardDTO getDashboardData() {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday   = startOfToday.plusDays(1);

        // -------------------------------
        // 1) KPI 카드 데이터
        // -------------------------------
        long todayOrders = workOrderRepository.countCreatedBetween(startOfToday, endOfToday);
        long inProgress  = workOrderRepository.countByStatus("IN_PROGRESS");
        long completed   = workOrderRepository.countByStatus("DONE");
        long delayed     = workOrderRepository.countDelayedOrders(LocalDateTime.now());

        // -------------------------------
        // 2) 공정 현황 테이블 데이터
        //    👉 기존 공정현황 목록 서비스 그대로 재사용
        // -------------------------------
        List<WorkOrderProcessDTO> processList =
                workOrderProcessService.getWorkOrderListForStatus();

        // -------------------------------
        // 3) 전체 Good, Defect 수량 합산
        // -------------------------------
        long goodTotal = processList.stream()
                .mapToLong(dto -> dto.getGoodQty() == null ? 0L : dto.getGoodQty())
                .sum();

        long defectTotal = processList.stream()
                .mapToLong(dto -> dto.getDefectQty() == null ? 0L : dto.getDefectQty())
                .sum();

        // -------------------------------
        // 4) 라인별 생산량 차트 (지금은 더미)
        // -------------------------------
        List<String> lineLabels = List.of("라인1","라인2","라인3","라인4","라인5","라인6");
        List<Long> lineSeries   = List.of(0L, 0L, 0L, 0L, 0L, 0L);

        // -------------------------------
        // 5) 최종 DTO 조립
        // -------------------------------
        return ProductionDashboardDTO.builder()
                .todayOrders(todayOrders)
                .inProgress(inProgress)
                .completed(completed)
                .delayed(delayed)
                .lineLabels(lineLabels)
                .lineSeries(lineSeries)
                .goodQtyTotal(goodTotal)
                .defectQtyTotal(defectTotal)   // 일단 0으로 내려보내기
                .processList(processList)
                .build();
    }

}
