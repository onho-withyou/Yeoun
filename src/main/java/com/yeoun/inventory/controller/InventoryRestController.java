package com.yeoun.inventory.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.inventory.dto.InventoryModalRequestDTO;
import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.dto.InventoryHistoryDTO;
import com.yeoun.inventory.dto.WarehouseLocationDTO;
import com.yeoun.inventory.entity.WarehouseLocation;
import com.yeoun.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/inventorys")
public class InventoryRestController {
	private final InventoryService inventoryService;
	
	// 재고리스트 조회
	@PostMapping("")
	public ResponseEntity<List<InventoryDTO>> inventorys(@RequestBody InventoryDTO inventoryDTO) {
		
		List<InventoryDTO> inventoryDTOList = inventoryService.getInventoryInfo(inventoryDTO);
		
		return ResponseEntity.ok(inventoryDTOList);
	}
	
	//창고 정보 조회
	@GetMapping("/locations")
	public ResponseEntity<List<WarehouseLocationDTO>> locations() {
		
		List<WarehouseLocationDTO> locationDTOList = inventoryService.getLocationInfo();
		
		return ResponseEntity.ok(locationDTOList);
	}
	
	//수량조절
	@PostMapping("/{ivId}/adjustQty")
	public ResponseEntity<Map<String, String>> adjustQty(
			@PathVariable("ivId") Long ivId, @RequestBody InventoryModalRequestDTO requestDTO,
			@AuthenticationPrincipal LoginDTO loginUser) {
		Map result = new HashMap<String, String>();
		String empId = loginUser.getEmpId();
		requestDTO.setIvId(ivId);
		
		inventoryService.adjustQty(requestDTO, empId);
		
		return ResponseEntity.ok(result);
	}
	
	//재고이동
	@PostMapping("/{ivId}/move")
	public ResponseEntity<Map<String, String>> moveInventory(
			@PathVariable("ivId") Long ivId, @RequestBody InventoryModalRequestDTO requestDTO,
			@AuthenticationPrincipal LoginDTO loginUser) {
		Map result = new HashMap<String, String>();
		String empId = loginUser.getEmpId();
		requestDTO.setIvId(ivId);
		
//		log.info(requestDTO);
		inventoryService.moveInventory(requestDTO, empId);
		
		return ResponseEntity.ok(result);
	}
	
	//재고폐기
	@PostMapping("/{ivId}/dispose")
	public ResponseEntity<Map<String, String>> disposeInventory(
			@PathVariable("ivId") Long ivId, @RequestBody InventoryModalRequestDTO requestDTO,
			@AuthenticationPrincipal LoginDTO loginUser) {
		Map result = new HashMap<String, String>();
		String empId = loginUser.getEmpId();
		requestDTO.setIvId(ivId);
		
		log.info(requestDTO);
		inventoryService.disposeInventory(requestDTO, empId);
		
		return ResponseEntity.ok(result);
	}
	
	//-----------------------------------------------------------------------------
	// 재고이력 정보
	//창고 정보 조회
	@GetMapping("/historys")
	public ResponseEntity<List<InventoryHistoryDTO>> historys() {
		
		List<InventoryHistoryDTO> historyDTOList = inventoryService.getInventoryHistorys();
		
		log.info("@@@@@@@@@@@@@@@@historyDTOList" + historyDTOList);
		
		return ResponseEntity.ok(historyDTOList);
	}
	
	
	
}



