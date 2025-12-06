package com.yeoun.masterData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
  
import com.yeoun.masterData.entity.MaterialMst;
import com.yeoun.masterData.entity.ProductMst;

@Repository
public interface ProductMstRepository extends JpaRepository<ProductMst, String> {

	//1. 완제품 조회
	//Optional<ProductMst> findByProductAll();
	//2. 완제품 수정
	//3. 완제품 삭제

}
