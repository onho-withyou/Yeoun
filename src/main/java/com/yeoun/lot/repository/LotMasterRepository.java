package com.yeoun.lot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.lot.entity.LotMaster;

@Repository
public interface LotMasterRepository extends JpaRepository<LotMaster, String> {

	// 최근 시퀀스 조회
    @Query(value = """
            SELECT 
                SUBSTR(lot_no, LENGTH(lot_no) - 2, 3) AS seq
            FROM LOT_MASTER
            WHERE 
                LOT_TYPE = :lotType
            AND PRD_ID = :prdId
            AND SUBSTR(lot_no, INSTR(lot_no, '-', 1, 3) + 1, 8) = :dateStr
            AND SUBSTR(lot_no, INSTR(lot_no, '-', 1, 4) + 1, INSTR(lot_no, '-', 1, 5) - INSTR(lot_no, '-', 1, 4) - 1) = :line
            ORDER BY seq DESC
            FETCH FIRST 1 ROWS ONLY
        """, nativeQuery = true)
	String findLastSeq(@Param("lotType") String lotType, 
			@Param("prdId") String prdId, 
			@Param("dateStr")String dateStr, 
			@Param("line") String line);

    // LOT 조회
	Optional<LotMaster> findByLotNo(String lotNo);

}
