package com.yeoun.common.service;

import org.springframework.stereotype.Service;

import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.leave.repository.LeaveHistoryRepository;
import com.yeoun.main.repository.ScheduleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@RequiredArgsConstructor
@Log4j2
public class FileAttachService {
	private final FileAttachRepository fileAttachRepository;
	
	// ---------------------------------------------------------------
	
	// 파일id로 파일 정보 가져오기
	public FileAttachDTO getFile(Long fileId) {
		FileAttach file = fileAttachRepository.findById(fileId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 파일입니다."));
		return FileAttachDTO.fromEntity(file);
	}
	
}
