package com.yeoun.masterData.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.ProdLine;

@Repository
public interface ProdLineRepository extends JpaRepository<ProdLine, String> {

	// 라인 3개 고정 출력용
    List<ProdLine> findAllByOrderByLineIdAsc();
    
}
