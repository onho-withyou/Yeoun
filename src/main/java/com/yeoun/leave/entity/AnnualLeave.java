package com.yeoun.leave.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.attendance.entity.WorkPolicy;
import com.yeoun.emp.entity.Emp;

import groovy.transform.ToString;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ANNUAL_LEAVE")
@SequenceGenerator(
		name = "ANNUAL_LEAVE_SEQ_GENERATOR",
		sequenceName = "ANNUAL_LEAVE_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AnnualLeave {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ANNUAL_LEAVE_SEQ_GENERATOR")
	private Long id;
	
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "EMP_ID", nullable = false)
	private Emp emp; // 직원 Id
	
	private LocalDate periodStart; // 입사일 기준일 경우 산정 시작일
	private LocalDate periodEnd; // 입사일 기준일 경우 산정 종료일
	private int useYear; // 회계연도 기준일 경우 기준연도
	
	@Column(nullable = false)
	private int totalDays; // 사원에게 부여된 총 연차
	
	private int usedDays; // 사용한 연차
	private int remainDays; // 남은 연차
	private String updatedUser; // 수기로 연차 수정한 직원
	private LocalDateTime updatedDate; // 수정된 날짜
	private String reason; // 수정한 이유
	
	@OneToMany(mappedBy = "annualLeave", cascade = CascadeType.ALL)
	private List<AnnualLeaveHistory> histories = new ArrayList<>();
	
	@Builder
	public AnnualLeave(Emp emp, LocalDate periodStart, LocalDate periodEnd, int totalDays) {
		this.emp = emp;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.useYear = periodStart.getYear();
		this.totalDays = totalDays;
		this.usedDays = 0;
		this.remainDays = totalDays;
	}
	
	// 연차 기준 정책에 따라 총 연차 계산
	public void updateTotaldays(WorkPolicy workPolicy) {
		LocalDate hireDate = emp.getHireDate();
		LocalDate today = LocalDate.now();
		
		if ("JOIN".equals(workPolicy.getAnnualBasis())) { // 입사일 기준
			this.totalDays = calculateJoinBasisAnnual(hireDate, today);
		} else { // 회계연도 기준
			this.totalDays = calculateFiscalBasisAnnual(hireDate, today);
		}
		
		// 잔여연차 계산
		this.remainDays = totalDays - usedDays;
	}
	
	// 입사일 기준 계산
	private int calculateJoinBasisAnnual(LocalDate hireDate, LocalDate today) {
		// 입사일부터 오늘 날짜까지의 연도 차이 계산
		long year = ChronoUnit.YEARS.between(hireDate, today);
		
		if (year < 1) { // 근속 연수 1년 미만이면, 입사월부터 매달 1일씩 연차 발생
			long month = ChronoUnit.MONTHS.between(hireDate, today);
			return (int) Math.min(month, 11); // 법적 최대치(11) 초과하지 않도록 제한
		} else {
			int extra = (int) ((year - 1) / 2); // 2년마다 1일씩 추가
			return Math.min(15 + extra, 25); // 최대 25일까지 부여
		}
	}
	
	// 회계연도 기준 계산
	private int calculateFiscalBasisAnnual(LocalDate hireDate, LocalDate today) {
		int year = today.getYear();
		LocalDate fiscalStart = LocalDate.of(year, 1, 1);
		// 입사일이 회계연도 시작일보다 언제인지 기준으로 근속연수 계산
		long years = ChronoUnit.YEARS.between(hireDate, fiscalStart);
		
		if (years < 1) { // 입사한 해의 연말까지의 근무 개월 수 계산
			long monthsWorked = ChronoUnit.MONTHS.between(hireDate, LocalDate.of(year, 12, 31));
			return (int) Math.min(monthsWorked, 11);
		} else {
			int extra = (int) ((years -1) / 2);
			return Math.min(15 + extra, 25); // 입사 후 1년 이상이면 기본 15일 + 근속에 따른 가산 부여
		}
	}
}
