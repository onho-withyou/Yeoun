package com.yeoun.notice.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.entity.Notice;
import com.yeoun.notice.repository.NoticeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
	private final NoticeRepository noticeRepository;
	
	//공지리스트 불러오기
	public List<NoticeDTO> getNotice(int page, int size) {
		
		Page<Notice> noticePage = noticeRepository.findAll(
				PageRequest.of(page, size, 
						Sort.by(Sort.Order.desc("noticeYN"), Sort.Order.desc("updatedDate"))));
		System.out.println(">>>>>>>>>" + noticePage.getPageable());
		List<Notice> noticeList = noticePage.getContent();
		
		return noticeList.stream()
				.map(notice -> NoticeDTO.fromEntity(notice))
				.collect(Collectors.toList());
	}
	
	// 공지상세 조회하기
	public NoticeDTO findById(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId).orElse(null);
				
		return NoticeDTO.fromEntity(notice);
	}

}
