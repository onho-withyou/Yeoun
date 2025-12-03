package com.yeoun.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.order.entity.WorkOrder;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, String> {
	
	// 공정 현황 - 진행중/지시 상태만 조회
	List<WorkOrder> findByStatusIn(List<String> statuses);

}
