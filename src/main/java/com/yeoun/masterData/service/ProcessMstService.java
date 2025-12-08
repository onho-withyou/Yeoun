package com.yeoun.masterData.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.repository.ProcessMstRepository;
import com.yeoun.masterData.repository.RouteHeaderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ProcessMstService {
	
	private final ProcessMstRepository processMstRepository;
	@Autowired
	private final RouteHeaderRepository routeHeaderRepository;
	
	//제품별공정 라우트 제품코드 드롭다운
	@Transactional(readOnly = true)
	public List<ProductMst> getPrdMst() {
		return routeHeaderRepository.findAllPrd();
	}
	
	//제품별 공정 라우트 그리드 조회
	@Transactional(readOnly = true)
	public List<ProcessMst> findAll(Map<String, Object> searchParams) {
		log.info("searchParams 조회된개수 - {}",searchParams);
		//return processMstRepository.findByProcessId();
		return null;
	}

}
