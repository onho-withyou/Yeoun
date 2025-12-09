package com.yeoun.masterData.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.masterData.entity.ProcessMst;

public interface ProcessMstRepository extends JpaRepository<ProcessMst, String> {

	// 공정 기준정보 조회
	Optional<ProcessMst> findByProcessId(String processId);

}
