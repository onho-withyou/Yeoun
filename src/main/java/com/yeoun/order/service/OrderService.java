package com.yeoun.order.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.order.dto.WorkOrderListDTO;
import com.yeoun.order.mapper.OrderMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {
	
	private final OrderMapper orderMapper;
	
	public List<WorkOrderListDTO> loadAllOrders (WorkOrderListDTO dto){
		return orderMapper.selectOrderList(dto);
	}
	

}









