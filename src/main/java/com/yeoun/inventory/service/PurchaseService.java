package com.yeoun.inventory.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.inventory.dto.MaterialOrderDTO;
import com.yeoun.inventory.dto.MaterialOrderItemDTO;
import com.yeoun.inventory.dto.SupplierDTO;
import com.yeoun.inventory.dto.SupplierItemDTO;
import com.yeoun.inventory.entity.MaterialOrder;
import com.yeoun.inventory.entity.MaterialOrderItem;
import com.yeoun.inventory.mapper.PurchaseMapper;
import com.yeoun.inventory.repository.MaterialOrderItemRepository;
import com.yeoun.inventory.repository.MaterialOrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PurchaseService {
	private final PurchaseMapper purchaseMapper;
	private final MaterialOrderRepository materialOrderRepository;
	private final MaterialOrderItemRepository materialOrderItemRepository;

	// 공급 업체 조회
	public List<SupplierDTO> findAllSuppliers() {
		return purchaseMapper.findAllSuppliers();
	}

	// 발주 등록
	@Transactional
	public void savePurchaseOrder(SupplierDTO supplierDTO, String empId) {
		// 발주 아이디 생성
		String orderId = generateOrderId();
		
		List<MaterialOrderItemDTO> items = new ArrayList<>();
		
		// 발주 테이블에 들어갈 총 금액 계산
		int totalAmount = 0;
		
		for (SupplierItemDTO item : supplierDTO.getSupplierItemList()) {
			int supply = item.getOrderAmount() * item.getUnitPrice(); // 공급가액
			log.info(">>>>>>>>>>>> supply : " + supply);
			int vat = (int) Math.round(supply * 0.1); // 부가세
			int total = supply + vat; // 품목별 총 금액
			
			totalAmount += total;
			
			MaterialOrderItemDTO materialOrderItemDTO = MaterialOrderItemDTO.builder()
					.orderId(orderId)
					.itemId(item.getItemId())
					.orderAmount((long) item.getOrderAmount())
					.unitPrice((long) item.getUnitPrice())
					.VAT((long) vat)
					.totalPrice((long) total)
					.supplyAmount((long) supply)
					.build();
			
			items.add(materialOrderItemDTO);
		}
		
		MaterialOrderDTO materialOrderDTO = MaterialOrderDTO.builder()
				.orderId(orderId)
				.clientId(supplierDTO.getClientId())
				.empId(empId)
				.dueDate(supplierDTO.getDueDate().toString())
				.totalAmount(String.valueOf(totalAmount))
				.items(items)
				.build();
		
		MaterialOrder materialOrder = materialOrderDTO.toEntity();
		
		for (MaterialOrderItemDTO itemDTO : items) {
			MaterialOrderItem orderItem = itemDTO.toEntity();
			
			materialOrder.addItem(orderItem);
		}
		
		log.info(">>>>>>>>>>>>>> materialOrder  : " + materialOrder);
		
		materialOrderRepository.save(materialOrder);
	}
	
	// 발주ID 생성
	private String generateOrderId() {
		String date = LocalDate.now().toString().replace("-", "");
		String pattern = "MAT" + date + "-%";
		
		// 오늘 날짜의 최대 seq 조회
		String maxId = materialOrderRepository.findMaxOrderId(pattern);
		
		int nextSeq = 1;
		
		// id가 존재할 경우 1씩 증가
		if (maxId != null) {
			String seqStr = maxId.substring(maxId.lastIndexOf("-") + 1);
			nextSeq = Integer.parseInt(seqStr) + 1;
		}
		
		String seqStr = String.format("%04d", nextSeq);
		
		return "MAT" + date + "-" + seqStr;
	}
	
	
}
