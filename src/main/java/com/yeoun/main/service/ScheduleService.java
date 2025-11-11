package com.yeoun.main.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.yeoun.emp.dto.DeptDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Employment;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.EmploymentRepository;
import com.yeoun.main.dto.ScheduleDTO;
import com.yeoun.main.entity.Schedule;
import com.yeoun.main.repository.ScheduleRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {
	private final ScheduleRepository scheduleRepository;
	private final DeptRepository deptRepository;
	private final EmpRepository empRepository;
	private final EmploymentRepository employmentRepository;
	// --------------------------------------------------
	
	//일정 등록모달 부서리스트 가져오기
	public List<DeptDTO> getDeptList() {
		List<Dept> deptList = deptRepository.findAll();
		
		return deptList.stream() //부서리스트를 엔터티 -> dto리스트로 변경후 리턴
				.map(dept -> DeptDTO.fromEntity(dept))
				.collect(Collectors.toList());
	}
	
	// 일정 등록로직
	public void createSchedule(@Valid ScheduleDTO scheduleDTO, Authentication authentication) {
		
		scheduleDTO.setCreatedUser(authentication.getName()); // 회원번호 입력
		
		if(!scheduleDTO.getAlldayYN().equals("Y")) {
			scheduleDTO.setAlldayYN("N"); // 종일 체크 없을경우 N으로 값변경
		}
		
		if(scheduleDTO.getScheduleType().equals("private")) {
			scheduleDTO.setScheduleType(authentication.getName()); // 일정타입이 개인일경우 회원번호 입력
		}
		
		Schedule schedule = scheduleDTO.toEntity();
		
		scheduleRepository.save(schedule);
	}
	
	// 일정 목록 조회로직
	public List<ScheduleDTO> getScheduleList(LocalDateTime startDate, LocalDateTime endDate, Authentication authentication) {
		String empId = authentication.getName();
		// empId로 로그인한 emp엔티티 정보 조회
		Emp emp = empRepository.findById(empId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 직원입니다.111"));
		
		Employment employment = employmentRepository.findByEmp(emp).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 직원입니다."));
		
		Dept myDept = employment.getDept();
		String myDeptId = myDept.getDeptId();
		String myDeptName = myDept.getDeptName();
		
		// 일정목록 조회
		List<Schedule> scheduleList = scheduleRepository.getIndividualSchedule(empId, myDeptId, startDate, endDate);
		// 일정목록 데이터 변환후 저장할 객체
		List<Schedule> scheduleList2 = new ArrayList<>();

		// scheduleType 판별후 보여줄 데이터로 변환
		if(!scheduleList.isEmpty()) {
			for(Schedule schedule : scheduleList) {
				String scheduleType = schedule.getScheduleType();
				if(scheduleType.equals("company")) {
					schedule.setScheduleType("회사");
				} else if(scheduleType.equals(authentication.getName())) {
					schedule.setScheduleType("개인");
				} else {
					Dept dept = deptRepository.findById(scheduleType).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 부서입니다."));
					schedule.setScheduleType(dept.getDeptName());
				}
				Emp scheduleEmp = empRepository.findById(schedule.getCreatedUser()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 직원입니다."));
				schedule.setCreatedUser(scheduleEmp.getEmpName());
				scheduleList2.add(schedule);
			}
			// 작성자 empId로 이름 조회후 변경 필요
			
		}
		
		return scheduleList2.stream().map(ScheduleDTO::fromEntity).collect(Collectors.toList());
	}

}

































