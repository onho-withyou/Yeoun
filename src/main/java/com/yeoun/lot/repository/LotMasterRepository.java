package com.yeoun.lot.repository;

import java.util.List;
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
		        REGEXP_SUBSTR(lot_no, '[0-9]{3}$') AS seq
		    FROM LOT_MASTER
		    WHERE 
		        LOT_TYPE = :lotType
		    AND PRD_ID = :prdId
		    AND SUBSTR(lot_no, INSTR(lot_no, '-', 1, 3) + 1, 8) = :dateStr
		    AND REGEXP_SUBSTR(lot_no, '-([0-9]{2})-', 1, 1, NULL, 1) = :line
		    ORDER BY seq DESC
		    FETCH FIRST 1 ROWS ONLY
		""", nativeQuery = true)
	String findLastSeq(@Param("lotType") String lotType, 
			@Param("prdId") String prdId, 
			@Param("dateStr")String dateStr, 
			@Param("line") String line);

    // LOT 조회
	Optional<LotMaster> findByLotNo(String lotNo);
	
	// 완제품 LOT 목록 조회 (LOT_TYPE = 'WIP', 'FIN' 인 LOT만 대상)
	// - LOT 추적에서 왼쪽 목록에 출력될 ROOT
	
	@Query("""
			select lm
			from LotMaster lm
			where lm.lotType in :lotTypes
			order by
			  case lm.currentStatus
			    when 'IN_PROCESS' then 0
			    when 'PROD_DONE'  then 1
			    when 'SCRAPPED'   then 2
			    else 3
			  end,
			  lm.createdDate desc
			""")
	List<LotMaster> findRootLotsOrdered(@Param("lotTypes") List<String> lotTypes);
	
	@Query(value = """
			WITH target_lot AS (
			  SELECT lm.lot_no, lm.prd_id, lm.created_date
			  FROM lot_master lm
			  WHERE lm.lot_type IN ('RAW','SUB','PKG')
			),
			latest_iv_id AS (
			  SELECT lot_no, MAX(iv_id) AS max_iv_id
			  FROM inventory
			  WHERE lot_no IN (SELECT lot_no FROM target_lot)
			  GROUP BY lot_no
			)
			SELECT
			  tl.lot_no AS lotNo,
			  CASE
			    WHEN p.prd_name IS NOT NULL THEN p.prd_name
			    WHEN m.mat_name IS NOT NULL THEN m.mat_name
			    ELSE tl.prd_id
			  END AS displayName,
			  COALESCE(inv.iv_status, 'SOLD_OUT') AS invStatus
			FROM target_lot tl
			LEFT JOIN product_mst p ON p.prd_id = tl.prd_id
			LEFT JOIN material_mst m ON m.mat_id = tl.prd_id
			LEFT JOIN latest_iv_id li ON li.lot_no = tl.lot_no
			LEFT JOIN inventory inv ON inv.iv_id = li.max_iv_id
			ORDER BY tl.created_date DESC
		    """, nativeQuery = true)
	List<MaterialRootRow> findMaterialRootLots();  // ✅ 올바른 반환 타입


}
