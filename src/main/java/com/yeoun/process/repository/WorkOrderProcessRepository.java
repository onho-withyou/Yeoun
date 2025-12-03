package com.yeoun.process.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.process.entity.WorkOrderProcess;

@Repository
public interface WorkOrderProcessRepository extends JpaRepository<WorkOrderProcess, String> {
	
    // 작업지시번호 기준 공정 전체 목록 조회
    List<WorkOrderProcess> findByWorkOrderOrderIdOrderByStepSeqAsc(String orderId);
    
    // 작업지시 + 단계순번으로 공정 1건 조회
    Optional<WorkOrderProcess> findByWorkOrderOrderIdAndStepSeq(String orderId, Integer stepSeq);
}
