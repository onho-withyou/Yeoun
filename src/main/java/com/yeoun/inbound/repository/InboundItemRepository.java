package com.yeoun.inbound.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yeoun.inbound.entity.InboundItem;

public interface InboundItemRepository extends JpaRepository<InboundItem, Long> {

	// 입고ID로 입고품목 조회
	List<InboundItem> findAllByInbound_InboundId(String inboundId);

	@Query("select i "
			+ "from InboundItem i "
			+ "where i.lotNo = :lotNo "
			+ "order by i.InboundItemId desc")
	Optional<InboundItem> findLatestByLotNo(@Param("lotNo") String lotNo);

	
}
