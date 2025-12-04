package com.yeoun.lot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.lot.entity.LotHistory;

@Repository
public interface LotHistoryRepository extends JpaRepository<LotHistory, Long> {

}
