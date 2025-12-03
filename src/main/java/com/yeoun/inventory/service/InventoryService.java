package com.yeoun.inventory.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.yeoun.inventory.dto.InventoryAdjustRequestDTO;
import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.dto.WarehouseLocationDTO;
import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.entity.InventoryHistory;
import com.yeoun.inventory.entity.WarehouseLocation;
import com.yeoun.inventory.repository.InventoryHistoryRepository;
import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.inventory.repository.WarehouseLocationRepository;
import com.yeoun.inventory.specification.InventorySpecs;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
	private final InventoryRepository inventoryRepository;
	private final InventoryHistoryRepository inventoryHistoryRepository;
	private final WarehouseLocationRepository warehouseLocationRepository;
	
	// 검색조건을 통해 재고리스트 조회
	public List<InventoryDTO> getInventoryInfo(InventoryDTO inventoryDTO) {
		
		Specification<Inventory> spec =
		        InventorySpecs.lotNoContains(inventoryDTO.getLotNo());
		
		spec = Specification.allOf(
		        spec,
		        InventorySpecs.prodNameContains(inventoryDTO.getProdName()),
		        InventorySpecs.itemTypeEq(inventoryDTO.getItemType()),
		        InventorySpecs.zoneEq(inventoryDTO.getZone()),
		        InventorySpecs.rackEq(inventoryDTO.getRack()),
		        InventorySpecs.statusEq(inventoryDTO.getStatus()),
		        InventorySpecs.ibDateGoe(inventoryDTO.getIbDate()),
		        InventorySpecs.expirationDateGoe(inventoryDTO.getExpirationDate())
		);
		
	    List<Inventory> list = inventoryRepository.findAll(spec);

	    return list.stream()
	               .map(InventoryDTO::fromEntity)
	               .toList();
	}
	
	//창고 로케이션 정보 불러오기 로직
	public List<WarehouseLocationDTO> getLocationInfo() {
		
		return warehouseLocationRepository.findAll().stream()
				.map(WarehouseLocationDTO::fromEntity)
				.toList();
	}
	
	// 재고 수량조절 로직
	@Transactional
	public void adjustQty(InventoryAdjustRequestDTO requestDTO, String empId) {
		Inventory inventory = inventoryRepository.findById(requestDTO.getIvId()).orElseThrow(() -> new EntityNotFoundException("존재하지않는 재고입니다."));
		
		Long prevInventoryQty = inventory.getIvAmount();
		Long changeInventoryQty = 0l;
		
		// 수량조절 유효성검사
		// 증가 감소 구분하여 변경할 Qty 값 설정
		if("INC".equals(requestDTO.getAdjustType())) {
			changeInventoryQty = prevInventoryQty + requestDTO.getAdjustQty();
			inventory.setIvAmount(changeInventoryQty);
		} else {
			changeInventoryQty = prevInventoryQty - requestDTO.getAdjustQty();
			// 변경후 수량이 0보다 작을경우
			if(changeInventoryQty < 0) {
				throw new IllegalArgumentException("변경 후 재고 수량이 0보다 작을 수 없습니다.");
			
			// 변경후 수량이 출고예정 수량보다 적은경우
			} else if(changeInventoryQty < inventory.getExpectObAmount()) {
				throw new IllegalArgumentException("변경 후 재고 수량이 출고예정 수량보다 작을 수 없습니다.");
				
			// 변경후 수량이 0인경우 재고삭제
			} else if(changeInventoryQty == 0l) {
				inventoryRepository.deleteById(inventory.getIvId());
			}
			// 변경수량 적용
			inventory.setIvAmount(changeInventoryQty);
		}
		
		// 재고내역 엔티티 작성
		InventoryHistory history = InventoryHistory.builder()
			.lotNo(inventory.getLotNo()) //재고의 lot번호
			.itemId(inventory.getItemId()) // 원자재 및 상품의 itemId(mat/prod)
			.prevLocationId(inventory.getWarehouseLocation().getLocationId())// 이전위치
			.currentLocationId(inventory.getWarehouseLocation().getLocationId()) // 현재위치
			.empId(empId) //작성자
			.workType(requestDTO.getAdjustType()) // 작업타입
			.prevAmount(prevInventoryQty) // 이전수량
			.currentAmount(changeInventoryQty) // 현재수량
			.reason(requestDTO.getReason()) // 이유
			.build();
		
		// 재고내역 등록
		inventoryHistoryRepository.save(history);
		
	}
	
	

}
