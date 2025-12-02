package com.yeoun.inventory.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.yeoun.inventory.dto.InventoryAdjustRequestDTO;
import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.inventory.specification.InventorySpecs;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
	private final InventoryRepository inventoryRepository;
	
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
	
	// 재고 수량조절 로직
	@Transactional
	public void adjustQty(InventoryAdjustRequestDTO requestDTO) {
		Inventory inventory = inventoryRepository.findById(requestDTO.getIvId()).orElseThrow(() -> new EntityNotFoundException("존재하지않는 재고입니다."));
		
		Long prevInventoryQty = inventory.getIvAmount();
		Long changeInventoryQty = 0l;
		
		// 증가 감소 구분하여 변경할 Qty 값 설정
		if("INC".equals(requestDTO.getAdjustType())) {
			changeInventoryQty = prevInventoryQty + requestDTO.getAdjustQty();
		} else {
			changeInventoryQty = prevInventoryQty - requestDTO.getAdjustQty();
		}
		
		inventory.setIvAmount(changeInventoryQty);
		
		// 수량조절 입력
		
		
		
	}

}
