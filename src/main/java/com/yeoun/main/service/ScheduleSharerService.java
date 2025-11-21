package com.yeoun.main.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.main.dto.ScheduleSharerDTO;
import com.yeoun.main.repository.ScheduleRepository;
import com.yeoun.main.repository.ScheduleSharerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleSharerService {
	private final ScheduleRepository scheduleRepository;
	private final ScheduleSharerRepository scheduleSharerRepository;
	// --------------------------------------------------
	public List<ScheduleSharerDTO> getSchedule(Long scheduleId) {
		
		return scheduleSharerRepository.findBySchedule_ScheduleId(scheduleId).stream()
										.map(ScheduleSharerDTO::fromEntity).toList();
	}
	
	


}

































