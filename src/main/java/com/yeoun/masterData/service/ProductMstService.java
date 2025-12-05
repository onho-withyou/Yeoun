package com.yeoun.masterData.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.repository.ProductMstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ProductMstService {
	
	private final ProductMstRepository productMstRepository;
	//1. 완제품 그리드 조회
	@Transactional(readOnly = true)
	public List<ProductMst> findAll() {
		log.info("productMstRepository.findAll() 조회된개수 - {}",productMstRepository.findAll());
		return productMstRepository.findAll();
	}

	//2. 완제품 그리드 수정
	//3. 완제품 그리드 삭제

}
