package com.yeoun.notice.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mysql.cj.log.Log;
import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.entity.Notice;
import com.yeoun.notice.repository.NoticeRepository;

import groovy.util.logging.Log4j2;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Log4j2
public class NoticeService {
	private final NoticeRepository noticeRepository;
	
	//공지리스트 불러오기
	public Page<NoticeDTO> getNotice(int page, int size, String searchKeyword, String orderKey, String orderMethod) {
		// 동적정렬 오름차순, 내림차순 결정
		Sort.Direction direction = "asc".equalsIgnoreCase(orderMethod) ? Sort.Direction.ASC : Sort.Direction.DESC;
		// 동적정렬 객체생성 (orderkey, orderMethod)
		Sort.Order dynamicOrder = new Sort.Order(direction, orderKey);
		
		// 기본 정렬 기준(고정여부, 수정일 내림차순) + 동적정렬기준 해서 정렬객체 생성
	    Sort sort = Sort.by(
            Sort.Order.desc("noticeYN"),
//            Sort.Order.desc("updatedDate"),
            dynamicOrder
        );
		
		// 페이징과 정렬을 포함하는 PageRequest 생성
		PageRequest pageRequest = PageRequest.of(page, size, sort);
		
		Page<Notice> noticePage;
		
		// 검색어가 존재하면 제목,내용에 포함된 공지사항 조회
	    if (searchKeyword != null && !searchKeyword.isEmpty()) {
	    	noticePage = noticeRepository.findByNoticeTitleContainingOrNoticeContentContaining(searchKeyword, searchKeyword, pageRequest);
	    } else { // 존재하지 않을 시 전체 공지사항 조회
	    	noticePage = noticeRepository.findAll(pageRequest);
	    }

	    return noticePage.map(NoticeDTO::fromEntity);
	}
	
	// 공지상세 조회하기
	public NoticeDTO findById(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId).orElse(null);
				
		return NoticeDTO.fromEntity(notice);
	}
	
	//공지사항 등록하기
	public void createNotice(NoticeDTO noticeDTO) {
		
		if(noticeDTO.getNoticeYN() == null) {
			noticeDTO.setNoticeYN("N");
		}
		
		noticeDTO.setCreatedUser(1102L);

		Notice notice = noticeDTO.toEntity();
		
		noticeRepository.save(notice);
	}
	
	//공지사항 수정하기
	@Transactional
	public void modifyNotice(@Valid NoticeDTO noticeDTO) {
		Long noticeId = noticeDTO.getNoticeId();
		Notice notice = noticeRepository.findById(noticeId).orElseThrow();
		
		notice.setNoticeTitle(noticeDTO.getNoticeTitle()); //변경된 제목 적용
		notice.setNoticeContent(noticeDTO.getNoticeContent()); //변경된 내용 적용
		
		if(noticeDTO.getNoticeYN() == null) {
			noticeDTO.setNoticeYN("N"); // 고정여부 값설정
		}
		notice.setNoticeYN(noticeDTO.getNoticeYN()); // 고정여부 변경값 적용
		// 트랜잭션이 끝날때 변경사항 자동 적용(더티체킹)
	}


}
