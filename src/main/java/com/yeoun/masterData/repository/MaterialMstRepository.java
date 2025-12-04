package com.yeoun.masterData.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.inventory.entity.MaterialOrder;
import com.yeoun.masterData.entity.MaterialMst;

public interface MaterialMstRepository extends JpaRepository<MaterialMst, String> {

	// 원자재 조회
	Optional<MaterialMst> findByMatId(String materialOrder);

}
