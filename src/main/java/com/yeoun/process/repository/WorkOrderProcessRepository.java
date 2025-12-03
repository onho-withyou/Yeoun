package com.yeoun.process.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.process.entity.WorkOrderProcess;

@Repository
public interface WorkOrderProcessRepository extends JpaRepository<WorkOrderProcess, String> {
	
    // 작업지시번호 기준 공정 전체 목록 조회
    List<WorkOrderProcess> findByWorkOrderOrderIdOrderByStepSeqAsc(String orderId);
}
