package com.yeoun.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.order.entity.WorkOrder;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, String> {
	
	// 공정 현황 - 진행중/지시 상태만 조회
	List<WorkOrder> findByStatusIn(List<String> statuses);

	// 가장 최근 작업지시 번호 조회
	Optional<WorkOrder> findTopByOrderIdStartingWithOrderByOrderIdDesc(String todayPrefix);

	// 해당 생산계획으로 생성한 workOrder 갯수 조회
	@Query(value = """
        SELECT COALESCE(SUM(w.PLAN_QTY), 0) 
            FROM WORK_ORDER w
            WHERE w.PLAN_ID = :planId
    """, nativeQuery = true)
	Integer sumWorkOrderQty(String planId);

}
