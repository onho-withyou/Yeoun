package com.yeoun.inbound.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.inbound.dto.InboundItemDTO;
import com.yeoun.inbound.repository.InboundRepository;
import com.yeoun.inventory.entity.MaterialOrder;
import com.yeoun.inventory.entity.MaterialOrderItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class InboundService {
	private final InboundRepository inboundRepository;
	
	// 입고대기 등록
	public void saveInbound(MaterialOrder materialOrder) {
		String date = LocalDate.now().toString().replace("-", "");
		String pattern = "INB" + date + "-%";
		
		// 오늘 날짜 기준 최대 seq 조회
		String maxId = inboundRepository.findMaxOrderId(pattern);
		
		// 입고 아이디 생성
		String inboundId = generateId(maxId, "INB", date);
		
		// 입고 품목 저장할 변수
		List<InboundItemDTO> items = new ArrayList<>();
		
		for (MaterialOrderItem item : materialOrder.getItems()) {
//			InboundItemDTO inboundItemDTO = InboundItemDTO.builder()
//					.lotNo("testLot")
//					.inboundId(inboundId)
//					.itemId(item.getItemId())
//					.requestAmount(item.getOrderAmount())
					
		}
	}
	
	// ID 생성
	// maxId : 오늘 날짜의 최대 seq 조회
	public String generateId(String maxId, String prefix, String date) {
		int nextSeq = 1;
		
		// id가 존재할 경우 1씩 증가
		if (maxId != null) {
			String seqStr = maxId.substring(maxId.lastIndexOf("-") + 1);
			nextSeq = Integer.parseInt(seqStr) + 1;
		}
		
		String seqStr = String.format("%04d", nextSeq);
		
		return prefix + date + "-" + seqStr;
	}
}
