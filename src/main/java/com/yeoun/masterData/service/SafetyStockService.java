package com.yeoun.masterData.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.SafetyStock;
import com.yeoun.masterData.repository.SafetyStockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class SafetyStockService {
	
	private final SafetyStockRepository safetyStockRepository;
	
	//1. 안전재고 그리드 조회
		@Transactional(readOnly = true)
		public List<SafetyStock> findAll() {
			log.info("safetyStockRepository.findAll() 조회된개수 - {}",safetyStockRepository.findAll());
			return safetyStockRepository.findAll();
		}

}
