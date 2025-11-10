package com.yeoun.main.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.yeoun.main.entity.Schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleDTO {
	private Long scheduleId; // 일정ID

	@NotBlank(message = "일정 제목은 필수 입력값입니다.")
	private String scheduleTitle; // 일정 제목
	
	@NotBlank(message = "일정 내용은 필수 입력값입니다.")
	private String scheduleContent; // 일정 내용
	
	@NotBlank(message = "일정 종류는 필수 입력값입니다.")
	private String scheduleType; // 일정 종류
	
	private Long createdUser; // 일정 등록자
	
	private String alldayYN; // 종일일정구분
	
	@NotNull(message = "일정 시작시간은 필수 입력값입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime scheduleStart; // 일정 시작시간
	
	@NotNull(message = "일정 마감시간은 필수 입력값입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime scheduleFinish; // 일정 마치는시간
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime createdDate; // 등록 일시
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime updatedDate; // 수정 일시
	
	
	// ----------------------------------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	public Schedule toEntity() {
		return modelMapper.map(this,  Schedule.class);
	}
	
	public static ScheduleDTO fromEntity(Schedule schedule) {
		return modelMapper.map(schedule, ScheduleDTO.class);
	}
}
