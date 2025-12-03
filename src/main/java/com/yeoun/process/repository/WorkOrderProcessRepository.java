package com.yeoun.process.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.process.dto.WorkOrderProcessDTO;
import com.yeoun.process.entity.WorkOrderProcess;

@Repository
public interface WorkOrderProcessRepository extends JpaRepository<WorkOrderProcess, String> {
	
	// 공정 현황 목록
    @Query("select new com.yeoun.process.dto.WorkOrderProcessDTO(" +
            " w.orderId, " +
            " p.prdId, " +      
            " p.prdName, " +    
            " w.planQty, " +
            " w.status " +
            ") " +
            "from WorkOrder w " +
            "join w.product p " +                 
            "where w.status in :statuses " +
            "order by w.startDate asc")
	List<WorkOrderProcessDTO> findWorkOrdersForProcessStatus(@Param("statues") List<String> statuses);

}
