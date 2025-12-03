package com.yeoun.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.entity.WarehouseLocation;

public interface InventoryRepository
	extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

	Optional<Inventory> findByWarehouseLocationAndLotNo(WarehouseLocation location, String lotNo);

}
