package com.yeoun.masterData.repository;

import java.util.List;
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
			SELECT *
			FROM PRODUCT_MST p
			-- prdId가 비어있거나 NULL이면 전체조회, 그렇지 않으면 포함(부분일치) 검색
			WHERE (:prdId IS NULL OR :prdId = '' OR p.PRD_ID LIKE '%' || :prdId || '%')
			AND (:prdName IS NULL OR :prdName = '' OR p.PRD_NAME LIKE '%' || :prdName || '%')
			""", nativeQuery = true)
	List<ProductMst> findByPrdIdList(@Param("prdId") String prdId, @Param("prdName") String prdName);

}
