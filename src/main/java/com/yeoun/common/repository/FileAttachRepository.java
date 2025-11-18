package com.yeoun.common.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.common.entity.FileAttach;

@Repository
public interface FileAttachRepository extends JpaRepository<FileAttach, Long> {
	
}
