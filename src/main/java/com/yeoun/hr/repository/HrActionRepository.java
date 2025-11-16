package com.yeoun.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.hr.entity.HrAction;

@Repository
public interface HrActionRepository extends JpaRepository<HrAction, Long> {
	
	// 인사 발령 목록
	List<HrAction> findAll();

}
