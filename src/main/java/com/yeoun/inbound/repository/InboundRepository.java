package com.yeoun.inbound.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yeoun.inbound.entity.Inbound;

public interface InboundRepository extends JpaRepository<Inbound, String> {

	// 오늘 날짜 기준으로 최대 seq 조회
	@Query(value = """
			SELECT MAX(i.inboundId)
			FROM Inbound i
			WHERE i.inboundId LIKE :pattern
			""")
	String findMaxOrderId(@Param("pattern") String pattern);
}
