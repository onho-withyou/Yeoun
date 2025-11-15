package com.yeoun.notice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notices")
public class NoticeRestController {
	
    private final NoticeService noticeService;
    
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDTO> getNotice(@PathVariable("noticeId")Long noticeId) {
        NoticeDTO noticeDTO = noticeService.getOneNotice(noticeId);
        
        System.out.println("@@@@@@@@@@@@@@@noticeDTO : " + noticeDTO);
        
        if (noticeDTO == null) { //찾는 공지사항이 없을때
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(noticeDTO);
    }
}
