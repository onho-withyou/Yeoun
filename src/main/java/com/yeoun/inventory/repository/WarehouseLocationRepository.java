package com.yeoun.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.entity.WarehouseLocation;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, String> {

	// zone과 rack으로 창고 조회
	List<WarehouseLocation> findByZoneAndRack(String zone, String rack);

}
