package com.yeoun.qc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.qc.entity.QcResult;

@Repository
public interface QcResultRepository extends JpaRepository<QcResult, String> {
	
	// 포장공정 시작 가능 여부 체크용 (품질 검사 PASS 여부)
	boolean existsByOrderIdAndOverallResult(String orderId, String overallResult);
	
	// 목록 화면에서 양품수량 가져오기 (작업지시당 1건)
    Optional<QcResult> findByOrderId(String orderId);

    // 여러 작업지시에 대한 QC 결과를 한 번에 조회
    List<QcResult> findByOrderIdIn(List<String> orderIds);
    
}
