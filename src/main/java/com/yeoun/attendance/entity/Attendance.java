package com.yeoun.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
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
@Table(name = "ATTENDANCE")
@SequenceGenerator(
		name = "ATTENDANCE_SEQ_GENERATOR",
		sequenceName = "ATTENDANCE_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Attendance {
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ATTENDANCE_SEQ_GENERATOR")
	private Long id;
	
	@Column(nullable = false, length = 7)
	private String empId; // 출근한 사원 번호
	
	@Column(nullable = false)
	@CreatedDate
	private LocalDate workDate; // 근무일자
	
	private LocalDateTime workIn; // 출근시간
	private LocalDateTime workOut; // 퇴근시간
	private Integer workDuration; // 근무시간(분단위)
	private String statusCode; // 근태상태
	private String remark; // 비고
	
	@Column(length = 7)
	private String createdUser; // 등록자(수기로 등록 시 사용)
	
	@CreatedDate
	private LocalDateTime createdDate; // 등록일자
	
	@Column(length = 7)
	private String updatedUser; // 수정자
	
	@CreatedDate
	private LocalDateTime updatedDate; // 수정일자
}












