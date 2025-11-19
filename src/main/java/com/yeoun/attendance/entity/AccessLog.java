package com.yeoun.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yeoun.emp.entity.Emp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ACCESS_LOG")
@SequenceGenerator(
		name = "ACCESS_LOG_SEQ_GENERATOR",
		sequenceName = "ACCESS_LOG_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AccessLog {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ACCESS_LOG_SEQ_GENERATOR")
	private Long accessId;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "EMP_ID", nullable = false)
	private Emp emp; // 외출한 사원 번호
	
	@Column(nullable = false)
	private LocalDate accessDate; // 출입 일자
	
	@JsonFormat(pattern = "HH:mm")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime outTime; // 외출 시간
	
	@JsonFormat(pattern = "HH:mm")
	@DateTimeFormat(pattern = "HH:mm")
	@Column(nullable = true)
	private LocalTime returnTime; // 복귀 시간
	
	@CreatedDate
	private LocalDateTime createdDate; // 등록 일시
	
	private String reason; // 외근 사유
	
	private String accessType; //외출 상태 구분 (OUT/IN/OUTWORK/ETC)
	
	@Builder
	public AccessLog(Emp emp, LocalDate accessDate, LocalTime outTime, LocalTime returnTime, String accessType) {
		this.emp = emp;
		this.accessDate = accessDate;
		this.outTime = outTime;
		this.returnTime = returnTime;
		this.accessType = accessType;
	}
	
	// 복귀 처리
	public void accessIn(LocalTime returnTime, String accessType) {
		this.returnTime = returnTime;
		this.accessType = accessType;
	}
	
	// 외출 처리
	public void accessOut(LocalTime outTime, String accessType) {
		this.outTime = outTime;
		this.accessType = accessType;
	}
}
