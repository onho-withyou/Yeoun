package com.yeoun.masterData.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.entity.RouteHeader;

@Repository
public interface RouteHeaderRepository extends JpaRepository<RouteHeader, String> {
	
	// 제품 + 사용여부로 라우트 찾기
	Optional<RouteHeader> findFirstByProductAndUseYn(ProductMst product, String useYn);

}
