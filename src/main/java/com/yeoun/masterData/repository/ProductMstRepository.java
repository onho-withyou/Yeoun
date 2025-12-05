package com.yeoun.masterData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.ProductMst;

@Repository
public interface ProductMstRepository extends JpaRepository<ProductMst, String> {

}
