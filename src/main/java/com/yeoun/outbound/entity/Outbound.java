package com.yeoun.outbound.entity;

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
@Table(name = "OUTBOUND")
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Outbound {
	@Id @Column(name = "OUTBOUND_ID", updatable = false)
	private String outboundId; 
	
	@Column(nullable = false)
	private String requestBy; // 요청자 empId
	
	@Column(nullable = true)
	private String processBy; // 출고처리자 empId
	
	@Column(nullable = true)
	private String workOrderId; // 작업지시서
	
	@Column(nullable = true)
	private String shipmentId; // 출하지시서
	
	@Column(nullable = false)
	private String status; // 상태
	
	@Column(nullable = false)
	private LocalDateTime expectOutboundDate; // 출고 예정일 
	
	@Column(nullable = true)
	private LocalDateTime outboundDate; // 출고일
	
	@CreatedDate
	private LocalDateTime createdDate; // 등록 일시
	
}
