package com.yeoun.outbound.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.outbound.entity.OutboundItem;

public interface OutboundItemRepository extends JpaRepository<OutboundItem, Long>{

	// 출고 품목 조회
	List<OutboundItem> findByOutbound_OutboundId(String outboundId);

}
