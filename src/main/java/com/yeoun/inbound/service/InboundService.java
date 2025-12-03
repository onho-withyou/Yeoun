package com.yeoun.inbound.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.yeoun.inbound.dto.InboundDTO;
import com.yeoun.inbound.dto.InboundItemDTO;
import com.yeoun.inbound.entity.Inbound;
import com.yeoun.inbound.entity.InboundItem;
import com.yeoun.inbound.repository.InboundRepository;
import com.yeoun.inventory.entity.MaterialOrder;
import com.yeoun.inventory.entity.MaterialOrderItem;
import com.yeoun.inventory.util.InventoryIdUtil;
import com.yeoun.masterData.entity.MaterialMst;
import com.yeoun.masterData.repository.MaterialMstRepository;
import com.yeoun.sales.entity.ClientItem;
import com.yeoun.sales.repository.ClientItemRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class InboundService {
	private final InboundRepository inboundRepository;
	private final ClientItemRepository clientItemRepository;
	private final MaterialMstRepository materialMstRepository;
	
	// 입고대기 등록
	@Transactional
	public void saveInbound(MaterialOrder materialOrder) {
		String date = LocalDate.now().toString().replace("-", "");
		String pattern = "INB" + date + "-%";
		
		// 오늘 날짜 기준 최대 seq 조회
		String maxId = inboundRepository.findMaxOrderId(pattern);
		
		// 입고 아이디 생성
		String inboundId = InventoryIdUtil.generateId(maxId, "INB", date);
		
		// 입고 품목 저장할 변수
		List<InboundItemDTO> items = new ArrayList<>();
		
		// 원재료의 유효기간(유통기한) 정보
		LocalDate datePlus = LocalDate.parse(materialOrder.getDueDate()).plusMonths(36);
		
		// 파라미터로 전달받은 발주 엔터티에서 발주 품목들 반복문으로 DTO로 변환
		for (MaterialOrderItem item : materialOrder.getItems()) {
			
			// 공급 품목 조회
			ClientItem clientItem = clientItemRepository.findByItemId(item.getItemId())
					.orElseThrow(() -> new NoSuchElementException("해당 품목 정보를 찾을 수 없습니다."));
			// 원자재 조회
			MaterialMst materialMst = materialMstRepository.findByMatId(item.getMaterialOrder())
					.orElseThrow(() -> new NoSuchElementException("해당 원재료 정보를 찾을 수 없습니다."));
			
			// 입고대기 품목 생성
			InboundItemDTO inboundItemDTO = InboundItemDTO.builder()
					.lotNo("testLot")
					.inboundId(inboundId)
					.itemId(clientItem.getMaterialId())
					.requestAmount(item.getOrderAmount())
					.inboundAmount(0L)
					.disposeAmount(0L)
					.itemType(materialMst.getMatType())
					.locationId(null)
					.manufactureDate(LocalDate.parse(materialOrder.getDueDate()).atStartOfDay())
					.expirationDate(datePlus.atStartOfDay())
					.build();

			items.add(inboundItemDTO);
		}
		
		// 입고 DTO 생성
		InboundDTO inboundDTO = InboundDTO.builder()
				.inboundId(inboundId)
				.expectArrivalDate(LocalDate.parse(materialOrder.getDueDate()).atStartOfDay())
				.inboundStatus("PENDING_ARRIVAL")
				.materialId(materialOrder.getOrderId())
				.items(items)
				.build();
		
		// 입고 DTO를 엔터티로 변환
		Inbound inbound = inboundDTO.toEntity();
		
		// 입고 품목들을 엔터티로 변환
		for (InboundItemDTO itemDTO : items) {
			InboundItem inboundItem = itemDTO.toEntity();
			
			inbound.addItem(inboundItem);
		}
		
		inboundRepository.save(inbound);
	}

	// 원재료 목록 데이터(날짜 지정과 검색 기능 포함)
	public List<InboundDTO> getMaterialInboundList(String startDate, String endDate, String keyword) {
		// TODO Auto-generated method stub
		return null;
	}
}
