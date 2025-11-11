package com.yeoun.main.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.emp.dto.DeptDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.repository.DeptRepository;
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
	
	// --------------------------------------------------
	
	//일정 등록모달 부서리스트 가져오기
	public List<DeptDTO> getDeptList() {
		List<Dept> deptList = deptRepository.findAll();
		
		return deptList.stream() //부서리스트를 엔터티 -> dto리스트로 변경후 리턴
				.map(dept -> DeptDTO.fromEntity(dept))
				.collect(Collectors.toList());
	}
	
	// 일정 등록로직
	public void createSchedule(@Valid ScheduleDTO scheduleDTO) {
		
		scheduleDTO.setCreatedUser(1102L);
		
		if(scheduleDTO.getAlldayYN() != "Y") {
			scheduleDTO.setAlldayYN("N");
		}
		Schedule schedule = scheduleDTO.toEntity();
		
		scheduleRepository.save(schedule);
	}
	
	// 일정 목록 조회로직
	public List<ScheduleDTO> getScheduleList(String startDate, String endDate) {
		// 일정목록 조회
		List<Schedule> scheduleList = scheduleRepository.findAll();
		
		
		// 일정목록 데이터 변환
		List<Schedule> scheduleList2 = new ArrayList<>();

		Long empId = 1102L;
		
		// scheduleType 판별후 보여줄 데이터로 변환
		for(Schedule schedule : scheduleList) {
			String scheduleType = schedule.getScheduleType();
			if(scheduleType.equals("company")) {
				schedule.setScheduleType("회사");
//			} else if(scheduleType.equals(String.valueOf(empId))) {
//				schedule.setScheduleType("개인");
			} else if(scheduleType.equals("empId")) {
				schedule.setScheduleType("개인");
			} else {
				Dept dept = deptRepository.findById(scheduleType).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 부서입니다."));
				schedule.setScheduleType(dept.getDeptName());
			}
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@ 변환된 entity" + schedule);
			scheduleList2.add(schedule);
		}
		// 작성자 empId로 이름 조회후 변경 필요
		
		return scheduleList2.stream().map(ScheduleDTO::fromEntity).collect(Collectors.toList());
	}

}

































