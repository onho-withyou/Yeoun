package com.yeoun.masterData.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Position;
import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.entity.RouteStep;
import com.yeoun.masterData.repository.ProcessMstRepository;
import com.yeoun.masterData.repository.ProductMstRepository;
import com.yeoun.masterData.repository.RouteHeaderRepository;
import com.yeoun.masterData.repository.RouteStepRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ProcessMstService {
	
	private final ProductMstRepository productMstRepository;
	private final ProcessMstRepository processMstRepository;
	private final RouteHeaderRepository routeHeaderRepository;
	private final RouteStepRepository routeStepRepository;
	
	//제품별공정 라우트 제품코드 드롭다운
	@Transactional(readOnly = true)
	public List<ProductMst> getPrdMst() {
		return routeHeaderRepository.findAllPrd();
	}
	// 제품별 공정 라우트 그리드 조회
	@Transactional(readOnly = true)
	public List<RouteHeader> getRouteHeaderList(String prdId, String routeName) {
		log.info("searchParams 조회된개수 - {}", prdId + routeName);
		
		return routeHeaderRepository.findByPrdIdAndRouteName(prdId, routeName);
	}
	// 공정코드 그리드 조회
	@Transactional(readOnly = true)
	public List<ProcessMst> getProcessCodeList() {
		log.info("getProcessCodeList 조회");
		return processMstRepository.findAll();
	}
	// 공정단계 그리드 조회
	@Transactional(readOnly = true)
	public List<RouteStep> getProcessStepList(String routeId) {
		log.info("getProcessStepList 조회 - {}", routeId);
		return routeStepRepository.findByRouteHeader_RouteIdOrderByStepSeqAsc(routeId);
	}
	// 공정단계 그리드 저장
	public String saveProcess(String empId, Map<String,Object> param) {
		log.info("productMstSaveList-Service------------>{}",param);
		try {

			//routeInfo 저장
			Object routeInfoObj = param.get("routeInfo");
			if (routeInfoObj instanceof Map) {
				Map<String,Object> routeInfo = (Map<String,Object>) routeInfoObj;
				RouteHeader routeHeader = RouteHeader.builder()
						.routeId(routeInfo.get("routeId").toString())
						.product(productMstRepository.findById(routeInfo.get("prdId").toString()
										).orElseThrow(() -> new IllegalArgumentException("product-prdId 없음: " + routeInfo.get("prdId").toString())))
						.routeName(routeInfo.get("routeName").toString())
						.useYn(routeInfo.get("useYn").toString())
						.description(routeInfo.get("description").toString())
						.createdId(empId)
						.createdDate(LocalDateTime.now())
						.build();
				routeHeaderRepository.save(routeHeader);
			}

			// createdRows
			Object createdObj = param.get("createdRows");
			if (createdObj instanceof List) {
				List<Map<String,Object>> created = (List<Map<String,Object>>) createdObj;
				for (Map<String,Object> row : created) {
					RouteStep routeStep = RouteStep.builder()
							.routeStepId(row.get("routeStepId").toString())
							.routeHeader(routeHeaderRepository.findById(row.get("routeId").toString())
											.orElseThrow(() -> new IllegalArgumentException("routeHeader-routeId 없음: " + row.get("routeId").toString())))
							.stepSeq(Integer.parseInt(row.get("stepSeq").toString()))
							.process(processMstRepository.findById(row.get("processId").toString())
											.orElseThrow(() -> new IllegalArgumentException("process-processId 없음: " + row.get("processId").toString())))
							.qcPointYn(row.get("qcPointYn").toString())	
							.createdId(empId)
							.createdDate(LocalDateTime.now())
							.build();
					routeStepRepository.save(routeStep);
				}
				
			}

			// updatedRows
			// Object updatedObj = param.get("updatedRows");
			// if (updatedObj instanceof List) {
			// 	@SuppressWarnings("unchecked")
			// 	List<Map<String,Object>> updated = (List<Map<String,Object>>) updatedObj;
			// 	java.util.List<String> missingIds = new ArrayList<>();
			// 	for (Map<String,Object> row : updated) {
			// 		Object idObj = row.get("prdId");
			// 		String prdId = (idObj == null) ? "" : String.valueOf(idObj).trim();

			// 		ProductMst target = null;
			// 		if (!prdId.isEmpty()) {
			// 			Optional<ProductMst> opt = productMstRepository.findById(prdId);
			// 			if (opt.isPresent()) target = opt.get();
			// 		}

			// 		// prdId가 비어있거나 조회 실패한 경우, itemName+prdName로 매핑을 시도
			// 		if (target == null) {
			// 			Object itemNameObj = row.get("itemName");
			// 			Object prdNameObj = row.get("prdName");
			// 			if (itemNameObj != null && prdNameObj != null) {
			// 				String itemName = String.valueOf(itemNameObj);
			// 				String prdName = String.valueOf(prdNameObj);
			// 				Optional<ProductMst> opt2 = productMstRepository.findByItemNameAndPrdName(itemName, prdName);
			// 				if (opt2.isPresent()) target = opt2.get();
			// 			}
			// 		}

			// 		if (target != null) {
			// 			// 기존 레코드 업데이트
			// 			applyMapToProduct(existingToMap(target), target, row);
			// 			target.setUpdatedId(empId);
			// 			productMstRepository.save(target);
			// 		} else {
			// 			// 존재하지 않는 prdId가 명시된 경우: PK 변경 시 의도치 않은 insert를 막기 위해 에러 처리
			// 			if (!prdId.isEmpty()) {
			// 				missingIds.add(prdId);
			// 				continue;
			// 			}
			// 			// prdId가 비어있고 매칭되는 기존 레코드가 없으면 새로 저장 (신규 추가 케이스)
			// 			ProductMst p = mapToProduct(row);
			// 			p.setCreatedId(empId);
			// 			productMstRepository.save(p);
			// 		}
			// 	}
			// 	if (!missingIds.isEmpty()) {
			// 		// 명시된 prdId들이 존재하지 않음: 롤백을 유도하고 에러 반환
			// 		throw new IllegalArgumentException("Unknown prdId(s) for update: " + String.join(",", missingIds));
			// 	}
			// }

			return "success";
		} catch (Exception e) {
			log.error("saveProductMst error", e);
			return "error: " + e.getMessage();
		}
	}

}

