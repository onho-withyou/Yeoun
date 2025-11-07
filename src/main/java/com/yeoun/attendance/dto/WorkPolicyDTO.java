package com.yeoun.attendance.dto;

import java.time.LocalTime;

import org.modelmapper.ModelMapper;

import com.yeoun.attendance.entity.WorkPolicy;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WorkPolicyDTO {
	private Long id;
	
	@NotNull
	private String inTime; // 출근 기준 시간
	
	@NotNull
	private String outTime; // 퇴근 기준 시간
	
	@NotNull
	private String lunchIn; // 점심 시작 시간
	
	@NotNull
	private String lunchOut; // 점심 종료 시간
	
	@NotNull
	@Min(0)
	@PositiveOrZero
	private Integer lateLimit; // 지각 유예 시간(분 단위)
	
	@NotNull
	private String annualBasis; // 연차 기준 설정 (회계연도 / 입사일 기준)
	
	// --------------------------------------------------
	// 유효성 검사
	@AssertTrue(message = "퇴근 시간은 출근 시간보다 늦어야 합니다.")
	public boolean isValidTimeRange() {
		if (inTime == null || outTime == null) return true;
		
		return LocalTime.parse(outTime).isAfter(LocalTime.parse(inTime));
	}
	
	@AssertTrue(message = "점심 종료 시간은 시작 시간보다 늦어야 합니다.")
	public boolean isValidLunchTimeRange() {
		if (lunchIn == null || lunchOut == null) return true;
		
		return LocalTime.parse(lunchOut).isAfter(LocalTime.parse(lunchIn));
	}
	
	// ------------------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public WorkPolicy toEntity() {
		return modelMapper.map(this, WorkPolicy.class);
	}
	
	// DTO 타입으로 변환
	public static WorkPolicyDTO fromEntity(WorkPolicy workPolicy) {
		return modelMapper.map(workPolicy, WorkPolicyDTO.class);
	}
}
