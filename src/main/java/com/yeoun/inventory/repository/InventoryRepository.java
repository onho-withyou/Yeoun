package com.yeoun.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.yeoun.inventory.entity.Inventory;

public interface InventoryRepository
	extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

}
