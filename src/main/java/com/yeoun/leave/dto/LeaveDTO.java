package com.yeoun.leave.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.leave.entity.AnnualLeave;

import groovy.transform.ToString;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
@Builder
public class LeaveDTO {
	private Long id; 
	private String empId; // 직원 Id
	private LocalDate periodStart; // 입사일 기준일 경우 산정 시작일
	private LocalDate periodEnd; // 입사일 기준일 경우 산정 종료일
	private int useYear; // 회계연도 기준일 경우 기준연도
	private int totalDays; // 사원에게 부여된 총 연차
	private int usedDays; // 사용한 연차
	private int remainDays; // 남은 연차
	private String updatedUser; // 수기로 연차 수정한 직원
	private LocalDateTime updatedDate; // 수정된 날짜
	private String reason; // 수정한 이유
	
	private String deptName; // 부서이름
	private String posName; // 직급명
	private String empName; // 직원 이름
	private BigDecimal leaveAllowanceAmount; //  연차수당추계액
	
	public void calculateAnnualLeaveAllowance(BigDecimal baseAmt, BigDecimal alwAmt) {
		
		if (baseAmt == null) {
			baseAmt = BigDecimal.ZERO;
		}
		
		if (alwAmt == null) {
			alwAmt = BigDecimal.ZERO;
		}
		
		// 월 통상임금 (기본급 + 수당)
		BigDecimal monthlyWage  = baseAmt.add(alwAmt);
		
		// 연차 수당 계산
		// ( 월 통상임금 / 209 ) * 8 * 잔여연차
		if (remainDays > 0 && monthlyWage.compareTo(BigDecimal.ZERO) > 0) { // 월 통상 임금이 0 보다 커야함
			BigDecimal dailyAllowance = monthlyWage
					.divide(BigDecimal.valueOf(209), 2, RoundingMode.HALF_UP) // 소수점 둘째 자리까지 
					.multiply(BigDecimal.valueOf(8));
			
			// 잔여 연차일수 곱하기
			BigDecimal totalAllowance = dailyAllowance
					.multiply(BigDecimal.valueOf(remainDays));
			
			this.leaveAllowanceAmount = totalAllowance;
		} else {
			this.leaveAllowanceAmount = BigDecimal.ZERO;
		}
	}
	
	// -------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public AnnualLeave toEntity() {
		return modelMapper.map(this, AnnualLeave.class);
	}
	
	// DTO 타입으로 변환 
	public static LeaveDTO fromEntity(AnnualLeave annualLeave) {
		return LeaveDTO.builder()
				.id(annualLeave.getId())
				.empId(annualLeave.getEmp().getEmpId())
				.periodStart(annualLeave.getPeriodStart())
				.periodEnd(annualLeave.getPeriodEnd())
				.usedDays(annualLeave.getUsedDays())
				.totalDays(annualLeave.getTotalDays())
				.useYear(annualLeave.getUseYear())
				.remainDays(annualLeave.getRemainDays())
				.updatedUser(annualLeave.getUpdatedUser())
				.updatedDate(annualLeave.getUpdatedDate())
				.reason(annualLeave.getReason())
				.build();
	}
}
