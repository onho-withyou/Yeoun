package com.yeoun.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yeoun.inventory.entity.MaterialOrder;

public interface MaterialOrderRepository extends JpaRepository<MaterialOrder, String> {

	// 오늘 날짜의 최대 seq 조회
	@Query(value = """
			SELECT MAX(m.orderId)
			FROM MaterialOrder m
			WHERE m.orderId LIKE :pattern
			""")
	String findMaxOrderId(@Param("pattern") String pattern);

}
