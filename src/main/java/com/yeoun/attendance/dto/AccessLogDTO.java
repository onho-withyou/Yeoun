package com.yeoun.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.modelmapper.ModelMapper;

import com.yeoun.attendance.entity.AccessLog;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AccessLogDTO {
	private Long accessId;
	
	@NotNull(message = "사원번호는 필수 입력값입니다")
	private String empId; // 외출한 사원 번호
	
	@NotNull
	private LocalDate accessDate; // 출입 일자
	
	private LocalTime outTime; // 외출 시간
	private LocalTime returnTime; // 복귀 시간
	private LocalDateTime createdDate; // 등록 일시
	private String reason; // 외근 사유
	private String accessType; //외출 상태 구분 (OUT/IN/OUTWORK/ETC)
	
	private String deptName; // 부서이름
	private String posName; // 직급명
	private String empName; // 직원 이름
	
	// ---------------------------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public AccessLog toEntity() {
		return modelMapper.map(this, AccessLog.class);
	}
	
	// DTO 타입으로 변환
	public static AccessLogDTO fromEntity(AccessLog accessLog) {
		AccessLogDTO accessLogDTO = modelMapper.map(accessLog, AccessLogDTO.class);
		accessLogDTO.setEmpName(accessLog.getEmp().getEmpName());
		accessLogDTO.setDeptName(accessLog.getEmp().getDept().getDeptName());
		accessLogDTO.setPosName(accessLog.getEmp().getPosition().getPosName());
		 
		return accessLogDTO;
	}

	
}
