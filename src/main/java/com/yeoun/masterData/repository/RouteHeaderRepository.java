package com.yeoun.masterData.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.entity.RouteHeader;

@Repository
public interface RouteHeaderRepository extends JpaRepository<RouteHeader, String> {
	
}
