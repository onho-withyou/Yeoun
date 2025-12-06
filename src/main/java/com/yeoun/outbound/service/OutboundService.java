package com.yeoun.outbound.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.dto.InventoryHistoryDTO;
import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.inventory.service.InventoryService;
import com.yeoun.inventory.util.InventoryIdUtil;
import com.yeoun.outbound.dto.OutboundDTO;
import com.yeoun.outbound.dto.OutboundItemDTO;
import com.yeoun.outbound.dto.OutboundOrderDTO;
import com.yeoun.outbound.dto.OutboundOrderItemDTO;
import com.yeoun.outbound.entity.Outbound;
import com.yeoun.outbound.entity.OutboundItem;
import com.yeoun.outbound.repository.OutboundRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OutboundService {
	private final OutboundRepository outboundRepository;
	private final InventoryService inventoryService;
	private final InventoryRepository inventoryRepository;

	// 출고 등록
	@Transactional
	public void saveOutbound(OutboundOrderDTO outboundOrderDTO, String empId) {
		String date = LocalDate.now().toString().replace("-", "");
		String pattern = "OUT" + date + "-%";
		
		// 오늘 날짜 기준 최대 seq 조회
		String maxId = outboundRepository.findMaxOrderId(pattern);
		
		// 출고 아이디 생성
		String outboundId = InventoryIdUtil.generateId(maxId, "OUT", date);
		
		// 출고 DTO 생성
		OutboundDTO outboundDTO = OutboundDTO.builder()
				.outboundId(outboundId)
				.requestBy(outboundOrderDTO.getCreatedId())
				.workOrderId(outboundOrderDTO.getWorkOrderId())
				.shipmentId(outboundOrderDTO.getShipmentId())
				.status("WAITING")
				.expectOutboundDate(outboundOrderDTO.getStartDate())
				.build();
		
		// 출고 품목 지정할 변수
		List<OutboundItemDTO> items = new ArrayList<>();
		
		// outboundOrderDTO에서 품목들 정보를 가져오기 위해 반복문 사용
		for (OutboundOrderItemDTO item : outboundOrderDTO.getItems()) {
			
			// MAT 타입이면 원재료 출고, FG이면 완제품 출고
			String itemId = "MAT".equals(outboundOrderDTO.getType())
					? item.getMatId()
					: item.getPrdId();
			
			// 출고 필요 수량
			Long requireQty = item.getOutboundQty();
			
			// 재고 조회
			List<Inventory> inventoryList = inventoryRepository.findByItemIdAndIvStatusNot(itemId, "EXPIRED");
			
			if (inventoryList.isEmpty()) {
				throw new IllegalArgumentException("재고가 없습니다.");
			}
			
			// FIFO 
			Long remaining = requireQty;
			
			for (Inventory stock : inventoryList) {
				if (remaining <= 0) break;
				
				Long available = stock.getIvAmount();
				
				if (available <= 0) continue;
				
				// 현재 재고가 필요한 양보다 많은 경우
				if (available > remaining) {
					// 재고 차감
					stock.setIvAmount(available - remaining);
					remaining = 0L;
				} else { // 재고가 부족한 경우 모두 사용
					remaining -= available;
					stock.setIvAmount(0L);
				}
				
				// 재고가 0이 되면 삭제
				if (stock.getIvAmount() == 0) {
					inventoryRepository.delete(stock);
				} else {
					inventoryRepository.save(stock);
				}
				
				// 재고 이력 남기기
				InventoryHistoryDTO inventoryHistoryDTO = InventoryHistoryDTO.builder()
						.lotNo(stock.getLotNo())
						.itemName(stock.getItemName())
						.empId(empId)
						.workType("OUTBOUND")
						.prevAmount(available)
						.currentAmount(remaining)
						.reason(outboundId)
						.currentLocationId(stock.getWarehouseLocation().getLocationId())
						.build();
				
				inventoryService.registInventoryHistory(inventoryHistoryDTO);
				
				// 출고품목 생성 로직
				OutboundItemDTO outboundItemDTO = OutboundItemDTO.builder()
						.outboundId(outboundId)
						.itemId(itemId)
						.lotNo(stock.getLotNo())
						.outboundAmount(requireQty)
						.itemType(stock.getItemType())
						.ivId(stock.getIvId())
						.build();
				
				items.add(outboundItemDTO);
				
			}
			
			// 필요한 수량을 채우지 못한 경우 
			if (remaining > 0) {
				throw new IllegalArgumentException("재고 부족");
			}
		}
		
		// 출고 DTO를 엔터티로 변환
		Outbound outbound = outboundDTO.toEntity();
		
		// 출고 품목들을 엔터티로 변환
		for (OutboundItemDTO itemDTO : items) {
			OutboundItem outboundItem = itemDTO.toEntity();
			
			outbound.addItem(outboundItem);
		}
		
		outboundRepository.save(outbound);
	}

}
