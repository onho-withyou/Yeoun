package com.yeoun.qc.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yeoun.masterData.entity.QcItem;
import com.yeoun.masterData.repository.QcItemRepository;
import com.yeoun.order.entity.WorkOrder;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.qc.dto.QcDetailRowDTO;
import com.yeoun.qc.dto.QcRegistDTO;
import com.yeoun.qc.entity.QcResult;
import com.yeoun.qc.entity.QcResultDetail;
import com.yeoun.qc.repository.QcResultDetailRepository;
import com.yeoun.qc.repository.QcResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QcResultService {
	
	private final QcResultRepository qcResultRepository;
    private final WorkOrderRepository workOrderRepository;
    private final QcItemRepository qcItemRepository;
    private final QcResultDetailRepository qcResultDetailRepository;
    
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
    	
    	// 로그인한 직원 ID 가져오기
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginEmpId = null;
        
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
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
        
        // 4) QC_RESULT_DETAIL 자동 생성
        createEmptyQcDetails(savedHeader, workOrder);

    	return savedHeader;
    }
    
    // QC_RESULT_DETAIL 자동 생성
    private void createEmptyQcDetails(QcResult qcHeader, WorkOrder workOrder) {

        // 0) 로그인한 직원 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginEmpId = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            loginEmpId = authentication.getName();
        }

        // 1) QC 항목 마스터 조회
        List<QcItem> qcItems =
                qcItemRepository.findByTargetTypeAndUseYnOrderBySortOrderAsc("FINISHED_QC", "Y");

        int seq = 1;

        for (QcItem item : qcItems) {

            QcResultDetail detail = new QcResultDetail();

            // (1) 상세 PK 생성 방식 (예시)
            // QCD-<QC_RESULT_ID>-001 형식
            String dtlId = String.format("QCD-%04d-%03d",
                    qcHeader.getQcResultId(),  // Long
                    seq++);

            detail.setQcResultDtlId(dtlId);

            // (2) 헤더 ID (Detail은 String이라 변환)
            detail.setQcResultId(String.valueOf(qcHeader.getQcResultId()));

            // (3) QC 항목ID
            detail.setQcItemId(item.getQcItemId());

            // (4) 측정값/판정/비고 초기화
            detail.setMeasureValue(null);   // 아직 미측정
            detail.setResult(null);         // PASS/FAIL은 나중에
            detail.setRemark(null);

            // (5) 등록자
            detail.setCreatedUser(loginEmpId);

            qcResultDetailRepository.save(detail);
        }
    }


    // -------------------------------------------------------------
    // QC 등록 목록
    public List<QcRegistDTO> getQcResultListForRegist() {
        return qcResultRepository.findRegistListByStatus("PENDING");
    }

    // QC 등록 모달 
	public List<QcDetailRowDTO> getDetailRows(Long qcResultId) {
		
		// QC 결과 ID 조회
		List<QcResultDetail> details = 
				qcResultDetailRepository.findByQcResultId(String.valueOf(qcResultId));
		
		List<QcDetailRowDTO> qcDetailRowDTO = new ArrayList<>();
		
		for (QcResultDetail d : details) {
			
			QcItem item = qcItemRepository.findById(d.getQcItemId())
					.orElse(null);
			
			QcDetailRowDTO qdrDTO = new QcDetailRowDTO();
			
			qdrDTO.setQcResultDtlId(d.getQcResultDtlId());
			qdrDTO.setQcItemId(d.getQcItemId());
			
			if(item != null) {
				qdrDTO.setItemName(item.getItemName());
				qdrDTO.setUnit(item.getUnit());
				qdrDTO.setStdText(item.getStdText());
			}
			
			qdrDTO.setMeasureValue(null);
			qdrDTO.setResult(null);
			qdrDTO.setRemark(null);
			
			qcDetailRowDTO.add(qdrDTO);
			
		}
		
		return qcDetailRowDTO;
	}

    
    
    
    
    
    
    
    
    
    
    
    

} // QcResultService 끝
