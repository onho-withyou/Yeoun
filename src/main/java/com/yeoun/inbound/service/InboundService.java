package com.yeoun.inbound.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.common.dto.DisposeDTO;
import com.yeoun.common.service.DisposeService;
import com.yeoun.inbound.dto.InboundDTO;
import com.yeoun.inbound.dto.InboundItemDTO;
import com.yeoun.inbound.dto.ReceiptDTO;
import com.yeoun.inbound.dto.ReceiptItemDTO;
import com.yeoun.inbound.entity.Inbound;
import com.yeoun.inbound.entity.InboundItem;
import com.yeoun.inbound.mapper.InboundMapper;
import com.yeoun.inbound.repository.InboundItemRepository;
import com.yeoun.inbound.repository.InboundRepository;
import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.dto.InventoryHistoryDTO;
import com.yeoun.inventory.entity.MaterialOrder;
import com.yeoun.inventory.entity.MaterialOrderItem;
import com.yeoun.inventory.repository.MaterialOrderRepository;
import com.yeoun.inventory.service.InventoryService;
import com.yeoun.inventory.util.InventoryIdUtil;
import com.yeoun.lot.dto.LotHistoryDTO;
import com.yeoun.lot.dto.LotMasterDTO;
import com.yeoun.lot.service.LotTraceService;
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
	private final InboundItemRepository inboundItemRepository;
	private final ClientItemRepository clientItemRepository;
	private final MaterialMstRepository materialMstRepository;
	private final MaterialOrderRepository materialOrderRepository;
	private final LotTraceService lotTraceService;
	private final InventoryService inventoryService;
	private final DisposeService disposeService;
	private final InboundMapper inboundMapper;
	
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
			MaterialMst materialMst = materialMstRepository.findByMatId(clientItem.getMaterialId())
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
				.prodId(null)
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
	public List<ReceiptDTO> getMaterialInboundList(LocalDateTime startDate, LocalDateTime endDate, String searchType, String keyword) {
		return inboundMapper.findAllMaterialInbound(startDate, endDate, searchType, keyword);
	}

	// 입고 상세 조회
	public ReceiptDTO getMaterialInbound(String inboundId) {
		return inboundMapper.findInbound(inboundId);
	}

	// 입고완료 처리(원재료)
	@Transactional
	public void updateInbound(ReceiptDTO receiptDTO, String empId) {
		// 입고 조회
		Inbound inbound = inboundRepository.findByinboundId(receiptDTO.getInboundId())
				.orElseThrow(() -> new NoSuchElementException("입고 내역을 찾을 수 없습니다."));
		
		// 발주 조회
		MaterialOrder materialOrder = materialOrderRepository.findByOrderId(inbound.getMaterialId())
				.orElseThrow(() -> new NoSuchElementException("발주 내역을 찾을 수 없습니다."));
		
		// 입고담당자 등록
		inbound.registEmpId(empId);
		
		// 입고 상태를 완료로 변경
		inbound.changeStatus("COMPLETED");
		// 발주 상태를 완료로 변경
		materialOrder.changeStatus("COMPLETED");
		
		Map<Long, InboundItem> inboundItemMap = inboundItemRepository
				.findAllByInbound_InboundId(receiptDTO.getInboundId())
				.stream()
				.collect(Collectors.toMap(InboundItem::getInboundItemId, item -> item));
		
		// 반복문 통해서 입고 품목 LOT 생성 및 수량 정보 업데이트
		for (ReceiptItemDTO itemDTO : receiptDTO.getItems()) {
			
			InboundItem inboundItem = inboundItemMap.get(itemDTO.getInboundItemId());
			
			log.info(">>>>>>>>>>>>>>>>>>>> itemID: " + itemDTO.getItemId());
			
			if (inboundItem == null) {
				throw new NoSuchElementException("입고 품목을 찾을 수 없습니다.");
			}
			
			Integer qty = itemDTO.getInboundAmount().intValue();
			
			// ----------------------------------------------------------------
			// LotMasterDTO 생성
			LotMasterDTO lotMasterDTO = LotMasterDTO.builder()
					.lotType(itemDTO.getItemType())
					.prdId(itemDTO.getItemId())
					.quantity(qty)
					.currentStatus("NEW")
					.currentLocType("WH")
					.currentLocId("WH" + itemDTO.getLocationId())
					.statusChangeDate(LocalDateTime.now())
					.build();
			
			// LOT 생성 및 LOT번호 반환
			String lotNo = lotTraceService.registLotMaster(lotMasterDTO, "00");
			
			// InboundItem 업데이트
			inboundItem.updateInfo(lotNo, itemDTO.getInboundAmount(), itemDTO.getDisposeAmount(), itemDTO.getLocationId());
			
			// lotHistory 생성
			LotHistoryDTO createLotHistoryDTO = LotHistoryDTO.builder()
					.lotNo(lotNo)
					.orderId("")
					.processId("")
					.eventType("CREATE")
					.status("NEW")
					.locationType("WH")
					.locationId("WH-" + itemDTO.getLocationId())
					.quantity(qty)
					.workedId(empId)
					.build();
			
			lotTraceService.registLotHistory(createLotHistoryDTO);
			
			// ------------------------------------------------------
			// 재고 등록
			InventoryDTO inventoryDTO = InventoryDTO.builder()
					.lotNo(lotNo)
					.locationId(itemDTO.getLocationId())
					.itemId(itemDTO.getItemId())
					.ivAmount(itemDTO.getInboundAmount())
					.expirationDate(inboundItem.getExpirationDate())
					.manufactureDate(inboundItem.getManufactureDate())
					.ibDate(LocalDateTime.now())
					.ivStatus("NORMAL")
					.expectObAmount(0L)
					.itemType(itemDTO.getItemType())
					.build();
			
			inventoryService.registInventory(inventoryDTO);
			
			log.info(">>>>>>>>>>>>>>>>>>>> itemID1 " + itemDTO.getItemId());
			
			// 재고이력 등록
			InventoryHistoryDTO inventoryHistoryDTO = InventoryHistoryDTO.builder()
					.lotNo(lotNo)
					.itemId(itemDTO.getItemId())
					.itemName(itemDTO.getItemName())
					.empId(empId)
					.workType("INBOUND")
					.prevAmount(0L)
					.currentAmount(itemDTO.getInboundAmount())
					.reason(receiptDTO.getInboundId())
					.currentLocationId(itemDTO.getLocationId())
					.build();
			
			inventoryService.registInventoryHistory(inventoryHistoryDTO);
			
			// ---------------------------------------------
			// 재고 등록 후 LOT HISTORY 업데이트
			// lotHistory 생성
			LotHistoryDTO updateLotHistoryDTO = LotHistoryDTO.builder()
					.lotNo(lotNo)
					.orderId("")
					.processId("")
					.eventType("RM_RECEIVE")
					.status("IN_STOCK")
					.locationType("WH")
					.locationId("WH-" + itemDTO.getLocationId())
					.quantity(qty)
					.workedId(empId)
					.build();
			
			lotTraceService.registLotHistory(updateLotHistoryDTO);
			// ------------------------------------------------------
			
			if (itemDTO.getDisposeAmount() > 0) {
				DisposeDTO dispose = DisposeDTO.builder()
						.lotNo(lotNo)
						.itemId(itemDTO.getItemId())
						.workType("INBOUND")
						.empId(empId)
						.disposeAmount(itemDTO.getDisposeAmount())
						.disposeReason("입고폐기")
						.build();
				
				disposeService.registDispose(dispose);
				
				Integer disposeQty = itemDTO.getDisposeAmount().intValue();
				
				// 폐기한 원재료 LOT 이력에 업데이트
				LotHistoryDTO disposeLotHistoryDTO = LotHistoryDTO.builder()
						.lotNo(lotNo)
						.orderId("")
						.processId("")
						.eventType("SCRAPPED")
						.status("SCRAPPED")
						.locationType("WH")
						.locationId("WH-" + itemDTO.getLocationId())
						.quantity(disposeQty)
						.workedId(empId)
						.build();
				
				lotTraceService.registLotHistory(disposeLotHistoryDTO);
			}
		}
		
	}
}
