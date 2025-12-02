package com.yeoun.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.inventory.dto.InventoryDTO;
import com.yeoun.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/inventorys")
public class InventoryRestController {
	private final InventoryService inventoryService;
	
	
	@PostMapping("")
	public ResponseEntity<List<InventoryDTO>> inventoryList(@RequestBody InventoryDTO inventoryDTO) {
		log.info("@@@@@@@@@@@@@@@@@@@@@@@@inventoryDTO : " + inventoryDTO);
		
		List<InventoryDTO> inventoryDTOList = inventoryService.getInventoryInfo(inventoryDTO);
		
		log.info("@@@@@@@@@@@@@inventoryDTOList : " + inventoryDTOList);
		
		return ResponseEntity.ok(inventoryDTOList);
	}
	
}
