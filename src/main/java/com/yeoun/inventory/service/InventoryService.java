package com.yeoun.inventory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.yeoun.inventory.dto.InventoryModalRequestDTO;
import com.yeoun.common.entity.Dispose;
import com.yeoun.common.repository.DisposeRepository;
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
	private final DisposeRepository disposeRepository;
	
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
	public void adjustQty(InventoryModalRequestDTO requestDTO, String empId) {
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
			.moveAmount(0l)
			.reason(requestDTO.getReason()) // 이유
			.build();
		
		// 재고내역 등록
		inventoryHistoryRepository.save(history);
		
	}
	
	
	// 재고 이동 로직
	@Transactional
	public void moveInventory(InventoryModalRequestDTO requestDTO, String empId) {
		// 현재 이동해야할 재고 정보
		Inventory inventory = inventoryRepository.findById(requestDTO.getIvId()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 재고입니다.")); 
		// 이동할 위치 
		WarehouseLocation location = warehouseLocationRepository.findById(requestDTO.getMoveLocationId().toString()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 위치입니다."));
		// 이동수량
		Long moveQty = requestDTO.getMoveAmount();
		// 이동요청 수량이 이동가능 수량보다 클때
	    Long availableQty = inventory.getIvAmount() - inventory.getExpectObAmount();
	    if (moveQty > availableQty) {
	        throw new IllegalArgumentException(
	            String.format("이동 불가능합니다. 현재 재고: %d, 출고예정: %d, 이동가능: %d, 요청수량: %d", 
	                inventory.getIvAmount(), inventory.getExpectObAmount(), availableQty, moveQty)
	        );
	    }
		// 이동요청 수량이 1보다 작을때
		if(moveQty < 1) {
	        throw new IllegalArgumentException("이동 수량은 1 이상이어야 합니다.");
		}
		
		// 이동위치에 이동하는 재고 lotNo 재고조회
		Optional<Inventory> existingInventoryOpt = inventoryRepository.findByWarehouseLocationAndLotNo(location, inventory.getLotNo());
		
		if(inventory.getIvAmount() == moveQty) {
			// 전체 재고 이동시 기존 재고 정보 삭제
			inventoryRepository.delete(inventory);
		} else { 
			// 부분이동시 재고수량 감소
			Long beforeAmount = inventory.getIvAmount();
			inventory.setIvAmount(beforeAmount - moveQty);
		}
		
		if(existingInventoryOpt.isPresent()) {
			//이동위치에 재고가 있으면 수량 합치기
			Inventory existingInventory = existingInventoryOpt.get();
			existingInventory.setIvAmount(existingInventory.getIvAmount() + moveQty);
			
			inventoryRepository.save(existingInventory);
			// 이력생성
			InventoryHistory history = InventoryHistory.createFromMove(inventory, existingInventory, moveQty, empId);
			inventoryHistoryRepository.save(history);
		} else { // 이동위치에 재고가 없으면  
			// 이동한 재고 생성
			Inventory moveInventory = inventory.createMovedInventory(location, moveQty);
			inventoryRepository.save(moveInventory);
			// 이력생성
			InventoryHistory history = InventoryHistory.createFromMove(inventory, moveInventory, moveQty, empId);
			inventoryHistoryRepository.save(history);
		}
		
	}
	
	// 재고 폐기 처리 
	@Transactional
	public void disposeInventory(InventoryModalRequestDTO requestDTO, String empId) {
		// 폐기처리할 재고 정보
		Inventory inventory = inventoryRepository.findById(requestDTO.getIvId()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 재고입니다."));
		// 폐기수량
		Long disposeQty = requestDTO.getDisposeAmount();
		// 이전 수량
		Long prevIvQty = inventory.getIvAmount();
		// 폐기수 재고 수량
		Long remainQty = prevIvQty - disposeQty;
		
		// 유효성검사
	    if (disposeQty > remainQty) {
	        throw new IllegalArgumentException(
	            String.format("폐기 불가능합니다. 현재 재고: %d, 출고예정: %d, 폐기가능: %d, 요청폐기: %d", 
	                inventory.getIvAmount(), inventory.getExpectObAmount(), remainQty, disposeQty)
	        );
	    }
		// 이동요청 수량이 1보다 작을때
		if(disposeQty < 1) {
	        throw new IllegalArgumentException("폐기 수량은 1 이상이어야 합니다.");
		}
		
		// 폐기수량이 재고의 전체 수량일 경우 재고 삭제
		if(disposeQty == inventory.getIvAmount()) {
			inventoryRepository.delete(inventory);
		} else { // 폐기후 남는 수량이 있을 경우
			inventory.setIvAmount(remainQty);
		}
		
		// 재고내역 엔티티 작성
		InventoryHistory history = InventoryHistory.builder()
			.lotNo(inventory.getLotNo()) //재고의 lot번호
			.itemId(inventory.getItemId()) // 원자재 및 상품의 itemId(mat/prod)
			.prevLocationId(inventory.getWarehouseLocation().getLocationId())// 이전위치
			.currentLocationId(inventory.getWarehouseLocation().getLocationId()) // 현재위치
			.empId(empId) //작성자
			.workType("DISPOSE") // 작업타입
			.prevAmount(prevIvQty) // 이전수량
			.currentAmount(remainQty) // 현재수량
			.moveAmount(0l)
			.reason(requestDTO.getReason()) // 이유
			.build();
		
		// 재고내역 등록
		inventoryHistoryRepository.save(history);
		
		//폐기테이블에 추가
		Dispose dispose = Dispose.builder()
				.disposeAmount(disposeQty)
				.disposeReason(requestDTO.getReason())
				.empId(empId)
				.itemId(inventory.getItemId())
				.lotNo(inventory.getLotNo())
				.workType("INVENTORY")
				.build();
		
		disposeRepository.save(dispose);
	}
	
	

}
