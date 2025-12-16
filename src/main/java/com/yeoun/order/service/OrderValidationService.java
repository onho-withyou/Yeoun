package com.yeoun.order.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.order.dto.WorkOrderValidateRequest;
import com.yeoun.order.dto.WorkOrderValidationResult;
import com.yeoun.order.mapper.OrderMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderValidationService {
	
	private final OrderMapper orderMapper;
	
	// =======================================================
	// 작업지시 등록 전 유효성 검증(라인)
	public boolean validateLineAvailability (WorkOrderValidateRequest req) {
		
		if (req.getLineId() == null || req.getLineId().isEmpty()) {
			return true;
		}
		
		int count = orderMapper.countLineOverlap(req);
		
		if (count > 0) {
			return false;
		}
		
		return true;
	}
	
	// =======================================================
	// 작업지시 등록 전 유효성 검증(작업자)
	public boolean validateWorkerAvailability (WorkOrderValidateRequest req) {
		
		if (req.getWorkers() == null || req.getWorkers().isEmpty()) {
			return true;
		}
		
		for (String workerId : req.getWorkers()) {
			req.setWorkerId(workerId);
			int count = orderMapper.countWorkerOverlap(req);
		
			if (count > 0) {
				return false;
			}
		}
		
		return true;
	}
	
	// =======================================================
	// 작업지시 등록 전 유효성 검증
	public WorkOrderValidationResult vaildateAll (WorkOrderValidateRequest req) {
		
		boolean lineAval	= validateLineAvailability(req);
		boolean workerAval  = validateWorkerAvailability(req); 
		
		WorkOrderValidationResult result = WorkOrderValidationResult.builder()
				.valid(lineAval && workerAval)
				.lineAvailable(lineAval)
				.workerAvailable(workerAval)
				.build();
		
		if (!lineAval) {
			result.setMessage("선택한 시간대에 해당 라인에서 진행하는 작업이 있습니다.");
		} else if (!workerAval) {
			result.setMessage("선택한 시간대에 해당 작업자가 진행하는 작업이 있습니다.");
		}
	
		// 추천은 실패한 경우에만 하기
		if (!lineAval) {
			result.setSuggestedLines(null);
		}
		
		if (!workerAval) {
			result.setSuggestedWorkers(null);
		}
		
		return result;
	}
	
	// =======================================================
	// 작업 가능 라인 제안
//	private List<String> suggestAvailableLines(WorkOrderValidateRequest req) {
//		if (req.getWorkers() == null || req.getWorkers().isEmpty()) {
//			return Collections.emptyList();
//		}
//		
//		List<String> allWorkers = empReository.
//	}
//	
//	// =======================================================
//	// 작업 가능 작업자 제안
//	private List<String> suggestAvailableWorkers(WorkOrderValidateRequest req) {
//		
//	}
	
	

}
