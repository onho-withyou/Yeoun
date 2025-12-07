package com.yeoun.outbound.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yeoun.outbound.dto.OutboundOrderDTO;

@Mapper
public interface OutboundMapper {
	
	// 출고 리스트 조회
	List<OutboundOrderDTO> findAllOutboundList(@Param("start") LocalDateTime start, 
			@Param("end") LocalDateTime end, @Param("keyword") String keyword);

	// 출고 상세페이지
	OutboundOrderDTO findOutbound(String outboundId);

}
