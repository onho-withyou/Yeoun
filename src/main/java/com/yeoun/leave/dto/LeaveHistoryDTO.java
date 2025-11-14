package com.yeoun.leave.dto;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;

import com.yeoun.leave.entity.AnnualLeaveHistory;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class LeaveHistoryDTO {
	private Long LeaveHistId;
	private Long emp_id; // 직원 id
	private String dept_id; // 부서 Id
	private Long leave_id; // 연차 Id
	private String leaveType; // 연차 종류 (연차/반차/병가) / (ANNUAL / HALF / SICK)
	private LocalDate startDate; // 사용 시작일
	private LocalDate endDate; // 사용 종료일
	private int usedDays; //  사용 일수
	private String reason; // 연차 사용 이유
	private String apprStatus; // 결재 상태 (승인/반려) / (APPROVED / REJECTED)
	private Long paymentId; // 결재문서 Id
	
	// --------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public AnnualLeaveHistory toEntity() {
		return modelMapper.map(this, AnnualLeaveHistory.class);
	}
	
	// DTO 타입으로 변환 
	
	public static LeaveHistoryDTO fromEntity(AnnualLeaveHistory annualLeaveHistory) {
		return modelMapper.map(annualLeaveHistory, LeaveHistoryDTO.class);
	}
}
