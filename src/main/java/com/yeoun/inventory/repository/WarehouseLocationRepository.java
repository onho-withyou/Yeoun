package com.yeoun.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.entity.WarehouseLocation;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, String> {

}
