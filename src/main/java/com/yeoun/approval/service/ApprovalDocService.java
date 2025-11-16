package com.yeoun.approval.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.approval.dto.ApprovalDocDTO;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.attendance.repository.AccessLogRepository;
import com.yeoun.attendance.repository.AttendanceRepository;
import com.yeoun.attendance.repository.WorkPolicyRepository;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.EmpRole;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.service.EmpService;

import groovy.util.logging.Log4j;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ApprovalDocService {
	
	private final ApprovalDocRepository approvalDocRepository;
	//기안자 명 불러오기
	@Transactional(readOnly = true)
	public List<Emp> getEmp() {
		// 사원 목록 조회
		return approvalDocRepository.findAllMember();
	}
	//부서목록조회
	@Transactional(readOnly = true)
	public List<Dept> getDept() {
		return approvalDocRepository.findAllDepartments();
	}
	//그리드 - 2.전체결재- 나와관련된 모든 결재문서
	@Transactional(readOnly = true)	
	public List<Object[]> getAllApprovalDocs(String empId) {
		return approvalDocRepository.findAllApprovalDocs(empId);
	}
	//그리드 - 3.내결재목록 - 내가 올린결재목록
	@Transactional(readOnly = true)	
	public List<Object[]> getMyApprovalDocs(String empId) {
		return approvalDocRepository.findMyApprovalDocs(empId);
	}

}
