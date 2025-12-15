package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
  
import com.yeoun.masterData.entity.MaterialMst;
import com.yeoun.masterData.entity.ProductMst;

@Repository
public interface ProductMstRepository extends JpaRepository<ProductMst, String> {

	//1. 완제품 조회
	//Optional<ProductMst> findByProductAll();
	//2. 완제품 수정
	//3. 완제품 삭제

	Optional<ProductMst> findByItemNameAndPrdName(String itemName, String prdName);

	// 제품ID로 조회
	Optional<ProductMst> findByPrdId(String prdId);
	
	@Query(value = """
				SELECT
				    p.*,
				    ec.emp_name AS created_by_name,
				    eu.emp_name AS updated_by_name
				FROM
				    PRODUCT_MST p
				LEFT JOIN
				    EMP ec
				ON
				    p.created_id = ec.emp_id
				LEFT JOIN
				    EMP eu
				ON
				    p.updated_id = eu.emp_id
				WHERE (:prdId IS NULL OR :prdId = '' OR p.PRD_ID LIKE '%' || :prdId || '%')
			AND (:prdName IS NULL OR :prdName = '' OR p.PRD_NAME LIKE '%' || :prdName || '%')
			AND (p.USE_YN = 'Y')
			""", nativeQuery = true)
	List<Map<String, Object>> findByPrdIdList(@Param("prdId") String prdId, @Param("prdName") String prdName);

}
