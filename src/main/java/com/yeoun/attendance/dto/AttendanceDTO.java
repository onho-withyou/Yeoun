package com.yeoun.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.modelmapper.ModelMapper;

import com.yeoun.attendance.entity.Attendance;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AttendanceDTO {
	private Long id;
	
	@NotNull(message = "사원번호는 필수 입력값입니다")
	private String empId; // 출근한 사원 번호
	
	@NotNull
	private LocalDate workDate; // 근무일자
	
	@NotNull(message = "출근시간은 필수 입력값입니다.")
	private LocalTime workIn; // 출근시간
	
	@NotNull(message = "퇴근시간은 필수 입력값입니다.")
	private LocalTime workOut; // 퇴근시간
	
	@Min(0)
	@PositiveOrZero
	private Integer workDuration; // 근무시간(분단위)
	
	@NotEmpty
	private String statusCode; // 근태상태
	
	private String remark; // 비고
	private String createdUser; // 등록자(수기로 등록 시 사용)
	private LocalDateTime createdDate; // 등록일자
	private String updatedUser; // 수정자
	private LocalDateTime updatedDate; // 수정일자
	
	// -----------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public Attendance toEntity() {
		return modelMapper.map(this, Attendance.class);
	}
	
	// DTO 타입으로 변환
	public static AttendanceDTO fromEntity(Attendance attendance) {
		return modelMapper.map(attendance, AttendanceDTO.class);
	}
}
