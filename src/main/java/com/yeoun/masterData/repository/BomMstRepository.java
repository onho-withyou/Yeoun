package com.yeoun.masterData.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.masterData.entity.BomMst;
import com.yeoun.masterData.entity.BomMstId;

public interface BomMstRepository extends JpaRepository<BomMst, BomMstId>{

}
