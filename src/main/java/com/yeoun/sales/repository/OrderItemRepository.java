package com.yeoun.sales.repository;

import com.yeoun.sales.entity.OrderItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByOrderId(String orderId);	
	

}
