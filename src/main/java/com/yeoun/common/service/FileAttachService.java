package com.yeoun.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;
import com.yeoun.common.repository.FileAttachRepository;
import com.yeoun.common.util.FileUtil;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.leave.repository.LeaveHistoryRepository;
import com.yeoun.main.repository.ScheduleRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@RequiredArgsConstructor
@Log4j2
public class FileAttachService {
	private final FileAttachRepository fileAttachRepository;
	private final FileUtil fileUtil;
	
	// ---------------------------------------------------------------
	
	// 파일id로 파일 정보 가져오기
	@Transactional
	public FileAttachDTO getFile(Long fileId) {
		FileAttach file = fileAttachRepository.findById(fileId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 파일입니다."));
		return FileAttachDTO.fromEntity(file);
	}
	// 단일 파일삭제
	@Transactional
	public void removeFile(Long fileId) {
		FileAttach file = fileAttachRepository.findById(fileId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 파일입니다."));
		
		fileAttachRepository.deleteById(fileId);
		
		fileUtil.deleteFile(FileAttachDTO.fromEntity(file));
	}
	// 전체파일삭제
	@Transactional
	public void removeFiles(List<FileAttach> fileList) {
		// DB삭제
		List<FileAttachDTO> fileDTOlist = fileList.stream().map(FileAttachDTO::fromEntity).toList();
		fileUtil.deleteFiles(fileDTOlist);
		// 파일삭제
		List<Long> fileIds = fileList.stream().map(FileAttach::getFileId).toList();
		fileAttachRepository.deleteAllById(fileIds);
	}
	
}
