package com.yeoun.notice.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "notice")
@SequenceGenerator(
		name = "NOTICES_SEQ_GENERATOR", // JPA 에서 사용하는 시퀀스 이름(DB의 시퀀스 이름이 아님!)
		sequenceName = "SCHEDULE_SEQ", // 오라클에서 사용하는 시퀀스 이름
		initialValue = 1, 			// 초기값(오라클 시퀀스의 start with 1과 동일)
		allocationSize = 1          // 증가값(오라클 시퀀스의 increment by 값과 동일)
		)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Notice {
	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBERS_SEQ_GENERATOR")
	@Column(updatable = false)
	private Long noticeId; // 공지 번호
	
	@Column(nullable = false)
	private String noticeTitle; // 공지제목
	
	@Column(nullable = false)
	private String noticeContent; // 공지 본문
	
	@Column(nullable = false, length = 1)
	private String noticeYN = "N"; // 공지 상단 배치여부

	
	@Column(nullable = false)
	private Long createdUser; // 공지 작성자
	
	@CreatedDate
	private LocalDateTime createdDate; // 공지 작성일
	
	@LastModifiedDate
	private LocalDateTime updatedDate; // 공지 수정일
	
	@Column(nullable = false, length = 1)
	private String deleteYN = "N"; // 공지 삭제 판별
}


























