package com.yeoun.equipment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.equipment.entity.ProdLine;

@Repository
public interface ProdLineRepository extends JpaRepository<ProdLine, String> {

	// 라인 3개 고정 출력용
    List<ProdLine> findAllByOrderByLineIdAsc();

    // 가장 마지막 라인넘버 찾기
    @Query(value = """
    SELECT MAX(TO_NUMBER(SUBSTR(LINE_ID, 4)))
    FROM PROD_LINE
    """,
    nativeQuery = true)
    Integer findMaxLineNumber();

    // Y혹은 N인것만 찾기
    List<ProdLine> findByUseYn(String useYn);
    
}
