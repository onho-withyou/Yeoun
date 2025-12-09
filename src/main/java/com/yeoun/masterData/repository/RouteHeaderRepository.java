package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.entity.RouteHeader;

@Repository
public interface RouteHeaderRepository extends JpaRepository<RouteHeader, String> {
	
	//제품별 공정 라우트
	//제품코드 관리드롭다운
	@Query(value ="""
			 SELECT * FROM product_mst
			""",nativeQuery = true)
	List<ProductMst> findAllPrd();
	
	// 제품별 공정라우터 조회
	//Optional<RouteHeader> findByProcessId(String prdId,String routeName);

}
