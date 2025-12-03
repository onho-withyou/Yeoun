package com.yeoun.process.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.order.entity.WorkOrder;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.process.dto.WorkOrderProcessDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderProcessService {
	
	private final WorkOrderRepository workOrderRepository;

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

}
