package com.yeoun.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
@EntityListeners(AuditingEntityListener.class)
public class AccessLog {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ACCESS_LOG_SEQ_GENERATOR")
	private Long id;
	
	@Column(nullable = false, length = 7)
	private String empId; // 외출한 사원 번호
	
	@Column(nullable = false)
	private LocalDate accessDate; // 출입 일자
	
	@JsonFormat(pattern = "HH:MM")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime outTime; // 외출 시간
	
	@JsonFormat(pattern = "HH:MM")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime returnTime; // 복귀 시간
	
	@CreatedDate
	private LocalDateTime createdDate; // 등록 일시
	
	private String reason; // 외근 사유
	
	private String accessType; //외출 상태 구분 (OUT/IN/OUTWORK/ETC)
}
