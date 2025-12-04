package com.yeoun.lot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.lot.entity.LotMaster;

@Repository
public interface LotMasterRepository extends JpaRepository<LotMaster, String> {

}
