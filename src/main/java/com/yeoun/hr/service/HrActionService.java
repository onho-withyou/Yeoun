package com.yeoun.hr.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Position;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.hr.dto.HrActionRequestDTO;
import com.yeoun.hr.entity.HrAction;
import com.yeoun.hr.repository.HrActionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrActionService {
	
	private final HrActionRepository hrActionRepository;
	private final EmpRepository empRepository;
	private final DeptRepository deptRepository;
	private final PositionRepository positionRepository;

	// 발령 등록
	@Transactional
	public Long createAction(HrActionRequestDTO dto) {
		
		// 0. 로그인 사용자 정보 가져오기
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginDTO loginUser = (LoginDTO) auth.getPrincipal();  
		String loginEmpId = loginUser.getEmpId();
		
		// 0-1. 등록자 Emp 조회
		Emp creator = empRepository.findById(loginEmpId)
		        .orElseThrow(() -> new IllegalArgumentException("등록자 사원을 찾을 수 없습니다. empId=" + loginEmpId));
		
		// 1. 대상 사원 조회
		Emp emp = empRepository.findById(dto.getEmpId())
					.orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다. empId=" + dto.getEmpId()));
		
		// 2. 발령 후 부서/직급 조회
		Dept toDpet = deptRepository.findById(dto.getToDeptId())
						.orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다. deptId=" + dto.getToDeptId()));
		
		Position toPos = positionRepository.findById(dto.getToPosCode())
							.orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다. posCode=" + dto.getToPosCode()));
		
		// 3. DTO -> 엔티티 (기본 필드 세팅)
	    HrAction action = dto.toEntity();
		
	    // 3-1. 관계 필드 세팅
		action.setEmp(emp);
		
		// 이전 부서 및 직급의 경우 현재 사원 정보에서 세팅
		action.setFromDept(emp.getDept());
		action.setFromPosition(emp.getPosition());
		
		// 이후 부서 및 직급의 경우 DTO에서 넘어온 값
		action.setToDept(toDpet);
		action.setToPosition(toPos);
		
		// 등록자 
		action.setCreatedUser(creator);
		
		// 4. 발령(HR_ACTION) 저장
		HrAction saved = hrActionRepository.save(action);
		return saved.getActionId();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
