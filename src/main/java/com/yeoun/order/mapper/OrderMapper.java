package com.yeoun.order.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.order.dto.WorkOrderListDTO;

@Mapper
@Repository
public interface OrderMapper {
	
	List<WorkOrderListDTO> selectOrderList (
			@Param("orderId")String orderId, @Param("productId")String productId, @Param("itemName")String itemName,
			@Param("planQty")Integer planQty, @Param("startTime")LocalDateTime startTime, @Param("endTime")LocalDateTime endTime,
			@Param("status")String status);

}
