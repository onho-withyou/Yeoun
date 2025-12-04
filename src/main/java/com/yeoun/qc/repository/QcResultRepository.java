package com.yeoun.qc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.qc.entity.QcResult;

@Repository
public interface QcResultRepository extends JpaRepository<QcResult, String> {
	
	// 특정 작업지시의 판정 결과가 PASS 여부
	boolean existsByOrderIdAndOverallResult(String orderId, String overallResult);

}
