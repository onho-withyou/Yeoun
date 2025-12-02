package com.yeoun.inventory.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.repository.InventoryRepository;
import com.yeoun.inventory.specification.InventorySpecs;

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

}
