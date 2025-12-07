package com.yeoun.masterData.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.BomMst;
import com.yeoun.masterData.mapper.BomMstMapper;
import com.yeoun.masterData.repository.BomMstRepository;
import com.yeoun.outbound.dto.OutboundOrderItemDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class BomMstService {
	private final BomMstRepository bomMstRepository;
	private final BomMstMapper bomMstMapper;
	
	//1. 완제품 그리드 조회
	@Transactional(readOnly = true)
	public List<BomMst> findAll() {
		log.info("bomMstRepository.findAll() 조회된개수 - {}",bomMstRepository.findAll());
		return bomMstRepository.findAll();
	}
	
	
	// =================================================================
	// prdId에 해당하는 BOM 리스트 조회
	public List<OutboundOrderItemDTO> getBomListByPrdId(String prdId) {
		return bomMstMapper.findByPrdIdList(prdId);
	}

}
