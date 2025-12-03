package com.yeoun.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.yeoun.inventory.entity.Inventory;
import com.yeoun.inventory.entity.InventoryHistory;

public interface InventoryHistoryRepository	extends JpaRepository<InventoryHistory, Long> {

}
