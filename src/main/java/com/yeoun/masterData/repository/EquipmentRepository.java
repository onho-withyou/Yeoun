package com.yeoun.masterData.repository;

import com.yeoun.masterData.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {
}
