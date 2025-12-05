package com.yeoun.masterData.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.approval.mapper.ApprovalDocMapper;
import com.yeoun.approval.mapper.ApprovalFormMapper;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.approval.repository.ApproverRepository;
import com.yeoun.approval.service.ApprovalDocService;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.common.util.FileUtil;
import com.yeoun.hr.repository.HrActionRepository;
import com.yeoun.leave.service.LeaveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MaterialMstService {
	//1. 원재료 그리드 조회

	//2. 원재료 그리드 수정
	
	//3. 원재료 그리드 삭제
	
}