package com.yeoun.common.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.common.entity.FileAttach;

@Repository
public interface FileAttachRepository extends JpaRepository<FileAttach, Long> {
	// 참조테이블, 참조테이블id로 파일조회
	List<FileAttach> findByRefTableAndRefId(String refTable, Long refId);
}
