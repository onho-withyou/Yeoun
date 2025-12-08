package com.yeoun.masterData.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.entity.QcItem;
import com.yeoun.masterData.repository.QcItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class QcItemService {
	private final QcItemRepository qcItemRepository;
	
	//조회
	@Transactional(readOnly = true)
	public List<QcItem> findAll() {
		return qcItemRepository.findAll();
	}
	//저장
	@Transactional
	public QcItem saveQcItem(String empId,QcItem qcItem) {
		qcItem.setCreatedId(empId);
		return qcItemRepository.save(qcItem);		
	}
	//삭제
	@Transactional
	public String deleteQcItem(List<String> qcItemIds) {
		log.info("qcItemRepository------------->{}",qcItemIds);
		try {
			//qcItemRepository.deleteAll(param);
			
			qcItemRepository.deleteAllById(qcItemIds);
			return "success";
		} catch (Exception e) {
			log.error("qcItemRepository error", e);
			return "error: " + e.getMessage();
		}
	} 

}
