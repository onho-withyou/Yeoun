package com.yeoun.attendance.entity;

import java.time.LocalDate;

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
@Table(name = "work_policy")
@SequenceGenerator(
		name = "WORK_POLICY_SEQ_GENERATOR",
		sequenceName = "WORK_POLICY_SEQ", 
		initialValue = 1,        
		allocationSize = 1    
)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class WorkPolicy {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "WORK_POLICY_SEQ_GENERATOR")
	private Long id;
	
	@Column(nullable = false, length = 5)
	private String inTime; // 출근 기준 시간
	
	@Column(nullable = false, length = 5)
	private String outTime; // 퇴근 기준 시간
	
	@Column(nullable = false, length = 5)
	private String lunchIn; // 점심 시작 시간
	
	@Column(nullable = false, length = 5)
	private String lunchOut; // 점심 종료 시간
	
	@Column(nullable = false, length = 2)
	private Integer lateLimit; // 지각 유예 시간(분 단위)
	
	@Column(nullable = false, length = 10)
	private String annualBasis; // 연차 기준 설정 (회계연도 / 입사일 기준)
	
	@CreatedDate
	private LocalDate createdDate;
	
	@LastModifiedDate
	private LocalDate updatedDate;
}
