package com.yeoun.inventory.entity;

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
@Table(name = "INBOUND")
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Inbound {
	@Id @Column(name = "INBOUND_ID", updatable = false)
	private String inboundId; 
	
	@Column(nullable = false)
	private LocalDateTime expectArrivalDate; // 예상도착일
	
	@Column(nullable = false)
	private String inboundStatus; // 입고상태
	
	@Column(nullable = true)
	private String materialId; // 발주 고유번호
	
	@Column(nullable = true)
	private String prodId; // 작업지시서 고유번호
	
	@CreatedDate
	private LocalDateTime createdDate; // 등록 일시
	
}
