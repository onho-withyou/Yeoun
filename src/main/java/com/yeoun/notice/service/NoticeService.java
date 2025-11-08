package com.yeoun.notice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.entity.Notice;
import com.yeoun.notice.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
	private final NoticeRepository noticeRepository;
	
	public List<NoticeDTO> getNotice() {
		
		List<Notice> noticeList = noticeRepository.findAll();
		
		return noticeList.stream()
				.map(notice -> NoticeDTO.fromEntity(notice))
				.collect(Collectors.toList());
	}

}
