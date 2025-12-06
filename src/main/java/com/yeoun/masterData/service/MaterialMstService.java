package com.yeoun.masterData.service;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.yeoun.masterData.entity.MaterialMst;
import com.yeoun.masterData.repository.MaterialMstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MaterialMstService {
	private final MaterialMstRepository materialMstRepository;
	
	//1. 원재료 그리드 조회
	@Transactional(readOnly = true)
	public List<MaterialMst> findAll() {
		return materialMstRepository.findAll();
	}

	//2. 원재료 그리드 수정
	
	//3. 원재료 그리드 삭제
	
}