package com.yeoun.hr.service;

import org.springframework.stereotype.Service;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Position;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.hr.dto.HrActionRequestDTO;
import com.yeoun.hr.entity.HrAction;
import com.yeoun.hr.repository.HrActionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrActionService {
	
	private final HrActionRepository hrActionRepository;
	private final EmpRepository empRepository;
	private final DeptRepository deptRepository;
	private final PositionRepository positionRepository;

	// 발령 등록
	public Long createAction(HrActionRequestDTO dto) {
		
		// 1. 대상 사원 조회
		Emp emp = empRepository.findById(dto.getEmpId())
					.orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다. empId=" + dto.getEmpId()));
		
		// 2. 발령 후 부서/직급 조회
		Dept toDpet = deptRepository.findById(dto.getToDeptId())
						.orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다. deptId=" + dto.getToDeptId()));
		
		Position toPos = positionRepository.findById(dto.getToPosCode())
							.orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다. posCode=" + dto.getToPosCode()));
		
		// 3. HrAction 엔티티 생성
		HrAction action = new HrAction();
		action.setEmp(emp);
		action.setActionType(dto.getActionType());
		action.setEffectiveDate(dto.getEffectiveDate());
		
		// 이전 부서 및 직급의 경우 현재 사원 정보에서 세팅
		action.setFromDept(emp.getDept());
		action.setFromPosition(emp.getPosition());
		
		// 이후 부서 및 직급의 경우 DTO에서 넘어온 값
		action.setToDept(toDpet);
		action.setToPosition(toPos);
		
		// 등록자 
		
		// 4. 저장
		HrAction saved = hrActionRepository.save(action);
				
		return saved.getActionId();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
