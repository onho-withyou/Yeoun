package com.yeoun.notice.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.notice.entity.Notice;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class NoticeDTO {
	private Long noticeId;
	@NotNull(message = "제목은 필수 입력 사항입니다.")
	private String noticeTitle;
	@NotNull(message = "내용은 필수 입력 사항입니다.")
	private String noticeContent;
	private String noticeYN;
	private Long createdUser;
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;
	
	private static ModelMapper modelMapper = new ModelMapper();
	
	public Notice toEntity() {
		return modelMapper.map(this,  Notice.class);
	}
	
	public static NoticeDTO fromEntity(Notice notice) {
		return modelMapper.map(notice, NoticeDTO.class);
	}
}
