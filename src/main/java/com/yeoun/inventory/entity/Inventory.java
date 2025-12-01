package com.yeoun.inventory.entity;

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
@Table(name = "INVENTORY")
@SequenceGenerator(
		name = "INVENTORY_SEQ_GENERATOR", // JPA 에서 사용하는 시퀀스 이름(DB의 시퀀스 이름이 아님!)
		sequenceName = "INVENTORY_SEQ", // 오라클에서 사용하는 시퀀스 이름
		initialValue = 1, 			// 초기값(오라클 시퀀스의 start with 1과 동일)
		allocationSize = 1          // 증가값(오라클 시퀀스의 increment by 값과 동일)
		)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Inventory {
	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INVENTORY_SEQ_GENERATOR")
	@Column(name = "IV_ID", updatable = false)
	private Long ivId; 
	
	@Column(nullable = false)
	private String lotNo;
	
	@Column(nullable = false)
	private String locationId;
	
	@Column(nullable = false)
	private String itemId; // 원자재/부자재, 완제품의 기준정보 고유값
	
	@Column(nullable = false)
	private Long ivAmount; // 재고량
	
	@Column(nullable = true)
	private LocalDateTime expirationDate; // 유통기한
	
	@Column(nullable = false)
	private LocalDateTime manufactureDate; // 제조일
	
	@Column(nullable = false)
	private LocalDateTime ibDate; // 입고일

	@Column(nullable = false)
	private Long expectObAmount = 0l; // 출고예정수량
	
	@Column(nullable = false)
	private String ivStatus; // 재고상태 : 정상/임박
	
	
	
	
	
	
	
	
	@Column(nullable = false)
	private String inboundStatus; // 입고상태
	
	@Column(nullable = true)
	private String materialId; // 발주 고유번호
	
	@Column(nullable = true)
	private String prodId; // 작업지시서 고유번호
	
	@CreatedDate
	private LocalDateTime createdDate; // 등록 일시
	
}
