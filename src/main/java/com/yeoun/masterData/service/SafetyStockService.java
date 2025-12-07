package com.yeoun.masterData.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.SafetyStock;
import com.yeoun.masterData.repository.SafetyStockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class SafetyStockService {
	
	private final SafetyStockRepository safetyStockRepository;
	
	//1. 안전재고 그리드 조회
		@Transactional(readOnly = true)
		public List<SafetyStock> findAll() {
			log.info("safetyStockRepository.findAll() 조회된개수 - {}",safetyStockRepository.findAll());
			return safetyStockRepository.findAll();
		}

	//2. 안전재고 그리드 저장
	public String saveSafetyStock(String empId, Map<String,Object> param) {
		log.info("safetyStockSaveList------------->{}",param);
		
			Object createdObj = param.get("createdRows");
			int createdCount = 0;
			if (createdObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> created = (List<Map<String,Object>>) createdObj;
				for (Map<String,Object> row : created) {
					SafetyStock s = mapToSafetyStock(row);
					safetyStockRepository.save(s);
					createdCount++;
				}
			}
			return "Created SafetyStock rows: " + createdCount;
	}

	//3. 안전재고 삭제 (itemId 목록으로 삭제)
	public String deleteSafetyStock(String empId, java.util.List<String> itemIds) {
		if (itemIds == null || itemIds.isEmpty()) return "No itemIds provided";
		int deleted = 0;
		for (String id : itemIds) {
			if (id == null) continue;
			if (safetyStockRepository.existsById(id)) {
				safetyStockRepository.deleteById(id);
				deleted++;
			}
		}
		return "Deleted SafetyStock rows: " + deleted;
	}

	// Map을 SafetyStock 엔티티로 변환하는 헬퍼 메서드
	private SafetyStock mapToSafetyStock(Map<String, Object> row) {
		SafetyStock s = new SafetyStock();

		if (row == null) return s;

		// 문자열 필드
		if (row.get("itemId") != null) s.setItemId(String.valueOf(row.get("itemId")).trim());
		if (row.get("itemType") != null) s.setItemType(String.valueOf(row.get("itemType")).trim());
		if (row.get("itemName") != null) s.setItemName(String.valueOf(row.get("itemName")).trim());
		if (row.get("itemUnit") != null) s.setItemUnit(String.valueOf(row.get("itemUnit")).trim());
		if (row.get("policyType") != null) s.setPolicyType(String.valueOf(row.get("policyType")).trim());
		if (row.get("status") != null) s.setStatus(String.valueOf(row.get("status")).trim());
		if (row.get("remark") != null) s.setRemark(String.valueOf(row.get("remark")).trim());

		// 숫자 필드: 널/빈 문자열/숫자 변환을 안전하게 처리
		Object vol = row.get("volume");
		if (vol != null) {
			Long v = parseLongSafely(vol);
			if (v != null) s.setVolume(v);
		}

		Object policyDays = row.get("policyDays");
		if (policyDays != null) {
			Long pd = parseLongSafely(policyDays);
			if (pd != null) s.setPolicyDays(pd);
		}

		Object safetyDaily = row.get("safetyStockQtyDaily");
		if (safetyDaily != null) {
			Long sd = parseLongSafely(safetyDaily);
			if (sd != null) s.setSafetyStockQtyDaily(sd);
		}

		Object safetyQty = row.get("safetyStockQty");
		if (safetyQty != null) {
			Long sq = parseLongSafely(safetyQty);
			if (sq != null) s.setSafetyStockQty(sq);
		}

		return s;
	}

	private Long parseLongSafely(Object o) {
		if (o == null) return null;
		if (o instanceof Number) return ((Number) o).longValue();
		String s = String.valueOf(o).trim();
		if (s.isEmpty()) return null;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			try {
				// 소수점이 있을 경우 소수 제거 후 파싱
				java.math.BigDecimal bd = new java.math.BigDecimal(s);
				return bd.longValue();
			} catch (Exception ex) {
				log.warn("Failed to parse long from value: {}", o);
				return null;
			}
		}
	}

}
