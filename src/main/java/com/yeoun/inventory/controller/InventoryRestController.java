package com.yeoun.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.inventory.dto.InventoryDTO;

import lombok.extern.log4j.Log4j2;


@RestController
@Log4j2
@RequestMapping("/api/inventorys")
public class InventoryRestController {
	
	@PostMapping("")
	public String inventoryList(@RequestBody InventoryDTO inventoryDTO) {
		System.out.println("dddddddddddddddddddddd");
		log.info("@@@@@@@@@@@@@@@@@@@@@@@@inventoryDTO : " + inventoryDTO);
		return new String();
	}
	
}
