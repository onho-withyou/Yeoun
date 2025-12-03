package com.yeoun.process.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.entity.RouteStep;
import com.yeoun.masterData.repository.RouteHeaderRepository;
import com.yeoun.masterData.repository.RouteStepRepository;
import com.yeoun.order.entity.WorkOrder;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.process.dto.WorkOrderProcessDTO;
import com.yeoun.process.dto.WorkOrderProcessDetailDTO;
import com.yeoun.process.dto.WorkOrderProcessStepDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderProcessService {
	
	private final WorkOrderRepository workOrderRepository;
	private final RouteHeaderRepository routeHeaderRepository;
	private final RouteStepRepository routeStepRepository;

	// 공정 현황 목록
	public List<WorkOrderProcessDTO> getWorkOrderListForStatus() {
		
		// 진행중/지시 상태만 (상태코드는 네 프로젝트 기준으로 맞춰)
		List<String> statuses = List.of("CREATED", "RELEASED");
		
		List<WorkOrder> workOrders = workOrderRepository.findByStatusIn(statuses);
		
		// 엔티티 -> DTO 매핑
		return workOrders.stream()
				.map(w -> new WorkOrderProcessDTO(
						w.getOrderId(),
						w.getProduct().getPrdId(),    
						w.getProduct().getPrdName(),  
						w.getPlanQty(),
						w.getStatus()
				))
				.toList();
	}

	
	// 공정 현황 상세
	@Transactional(readOnly = true)
	public WorkOrderProcessDetailDTO getWorkOrderProcessDetail(String orderId) {

	    // 1) 작업지시 기본정보
	    WorkOrder workOrder = workOrderRepository.findById(orderId)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작업지시: " + orderId));

	    // 2) 제품별 라우트 찾기
	    RouteHeader routeHeader = routeHeaderRepository
	            .findFirstByProductAndUseYn(workOrder.getProduct(), "Y")
	            .orElseThrow(() -> new IllegalArgumentException("해당 제품의 사용중인 라우트가 없습니다."));

	    // 3) 라우트 단계 목록 조회
	    List<RouteStep> steps = routeStepRepository
	            .findByRouteHeaderOrderByStepSeqAsc(routeHeader);

	    // 4) 상단 요약 DTO (workOrder 기반)
	    WorkOrderProcessDTO wopDTO = new WorkOrderProcessDTO(
	            workOrder.getOrderId(),
	            workOrder.getProduct().getPrdId(),
	            workOrder.getProduct().getPrdName(),
	            workOrder.getPlanQty(),
	            workOrder.getStatus()
	    );

	    // 5) 공정 단계 DTO 리스트
	    List<WorkOrderProcessStepDTO> stepDTOs = steps.stream()
	            .map(s -> new WorkOrderProcessStepDTO(
	                    s.getStepSeq(),
	                    s.getProcess().getProcessId(),
	                    s.getProcess().getProcessName(),
	                    "READY",   // 임시 상태
	                    null,      // startTime
	                    null,      // endTime
	                    null,      // goodQty
	                    null,      // defectQty
	                    s.getRemark()
	            ))
	            .toList();

	    return new WorkOrderProcessDetailDTO(wopDTO, stepDTOs);
	}

	

} // WorkOrderProcessService 끝
