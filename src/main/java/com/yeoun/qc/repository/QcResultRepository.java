package com.yeoun.qc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.qc.entity.QcResult;

@Repository
public interface QcResultRepository extends JpaRepository<QcResult, String> {
	
	// 특정 작업지시의 판정 결과가 PASS 여부
	boolean existsByOrderIdAndOverallResult(String orderId, String overallResult);
	
	// 작업지시별 최종 QC 결과 단건 조회
    Optional<QcResult> findByOrderId(String orderId);

}
