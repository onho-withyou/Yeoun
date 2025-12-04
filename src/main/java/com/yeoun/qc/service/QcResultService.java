package com.yeoun.qc.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.yeoun.order.entity.WorkOrder;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.qc.entity.QcResult;
import com.yeoun.qc.repository.QcResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QcResultService {
	
	private final QcResultRepository qcResultRepository;
    private final WorkOrderRepository workOrderRepository;
    
    // --------------------------------------------------------------
    // 캡/펌프 공정 종료 시 호출되는 QC 결과 생성 메서드
    // - 이미 해당 작업지시의 QC_RESULT가 있으면 재생성하지 않고 그대로 반환
    // - 없으면 "검사대기(PENDING)" 상태의 헤더(및 필요시 디테일) 생성
    public QcResult createPendingQcResultForOrder(String orderId) {
    	
    	// 이미 생성된 QC 결과가 있으면 재사용
    	return qcResultRepository.findByOrderId(orderId)
    			.orElseGet(() -> createNewPendingQcResult(orderId));
    }
    
    // 새로운 QC_RESULT(+DETAIL)을 생성하는 메서드
    private QcResult createNewPendingQcResult(String orderId) {
    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginEmpId = null;
        
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            // username 이 사번(EMP_ID)이면 이렇게
            loginEmpId = authentication.getName();
        }
    	
        // 1) 작업지시 조회
        WorkOrder workOrder = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작업지시: " + orderId));

        // 2) QC_RESULT 헤더 엔티티 생성
        QcResult qc = new QcResult();

        qc.setOrderId(orderId);                         

        // LOT 번호가 이미 있다면 세팅 (없으면 null/나중에 업데이트)
        // qc.setLotNo(workOrder.getLotNo());

        // 검사 수량: 일단 계획수량 기준으로 세팅 (나중에 필요시 수정 가능)
        qc.setInspectionQty(workOrder.getPlanQty());

        // 검사일시는 아직 미정 ->  QC 완료 시점에 세팅
        qc.setInspectionDate(null);

        // 초기 결과 상태: PENDING (또는 null)
        qc.setOverallResult("PENDING");

        // 초기 양품/불량 수량은 0으로
        qc.setGoodQty(0);
        qc.setDefectQty(0);

        // 비고/실패사유 등은 아직 없음
        qc.setFailReason(null);
        qc.setRemark("자동생성 - 캡/펌프 공정 종료에 따른 QC 대기");

        // 등록자
        qc.setCreatedId(loginEmpId);
        
        // 3) 헤더 저장
        QcResult savedHeader = qcResultRepository.save(qc);

    	return savedHeader;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

} // QcResultService 끝
