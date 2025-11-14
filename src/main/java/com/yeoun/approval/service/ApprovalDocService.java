package com.yeoun.approval.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.approval.dto.ApprovalDocDTO;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.attendance.repository.AccessLogRepository;
import com.yeoun.attendance.repository.AttendanceRepository;
import com.yeoun.attendance.repository.WorkPolicyRepository;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.service.EmpService;

import groovy.util.logging.Log4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApprovalDocService {
	
	private final ApprovalDocRepository approvalDocRepository;
	private final EmpRepository empRepository;

	//기안자 명 (사번) 정보 조회
	public List<EmpListDTO> getEmpList() {
		log.info("▶>>>>>>>>>>> 조회");
		List<Emp> empList = empRepository.findAll();
		
		return empRepository.findAllForList();
	}

}
