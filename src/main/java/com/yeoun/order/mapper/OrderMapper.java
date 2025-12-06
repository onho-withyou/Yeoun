package com.yeoun.order.mapper;

import java.util.List;

import com.yeoun.order.dto.WorkOrderSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.order.dto.WorkOrderListDTO;

@Mapper
@Repository
public interface OrderMapper {
	
	// 작업지시 리스트 모두 불러오기
	List<WorkOrderListDTO> selectOrderList (WorkOrderSearchDTO dto);
	
	// 작업자 리스트
	List<EmpListDTO> selectWorkers (String pos);

}
