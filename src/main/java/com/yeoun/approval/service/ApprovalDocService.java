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
import com.yeoun.approval.dto.ApprovalFormDTO;
import com.yeoun.approval.entity.ApprovalForm;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.attendance.repository.AccessLogRepository;
import com.yeoun.attendance.repository.AttendanceRepository;
import com.yeoun.attendance.repository.WorkPolicyRepository;
import com.yeoun.auth.dto.LoginDTO;
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
		return approvalDocRepository.findAllMember();
	}
	//기안자명 불러오기
	@Transactional(readOnly = true)
	public List<Object[]> getEmp2() {
		return approvalDocRepository.findAllMember2();
	}
	//기안서 양식종류
	@Transactional(readOnly = true)
	public List<ApprovalForm> getFormTypes(String deptId) {
		return approvalDocRepository.findAllFormTypes(deptId);
	}
	//부서목록조회
	@Transactional(readOnly = true)
	public List<Dept> getDept() {
		return approvalDocRepository.findAllDepartments();
	}
	//그리드 - 1.결재사항 - 진행해야할 결재만 - 결재권한자만 볼수있음
	@Transactional(readOnly = true)	
	public List<Object[]> getPendingApprovalDocs(String empId) {
		return approvalDocRepository.findPendingApprovalDocs(empId);
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
	//그리드 - 4.결재대기 - 나와관련된 모든 결재문서
	@Transactional(readOnly = true)
	public List<Object[]> getWaitingApprovalDocs(String empId) {
		return approvalDocRepository.findWaitingApprovalDocs(empId);
	}	
	//그리드 - 5.결재완료 - 결재권한자가 결재를 완료하면 볼수 있음(1차,2차,3차 모든결재 완료시)
	 @Transactional(readOnly = true)		
	 public List<Object[]> getFinishedApprovalDocs(String empId) {
	 	return approvalDocRepository.findFinishedApprovalDocs(empId);
	 }

}
