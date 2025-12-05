package com.yeoun.masterData.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.masterData.entity.BomMst;
import com.yeoun.masterData.entity.MaterialMst;

public interface BomMstRepository extends JpaRepository<BomMst, String>{

}
