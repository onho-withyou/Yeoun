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
@Table(name = "INBOUND_ITEM")
@SequenceGenerator(
		name = "INBOUND_ITEM_GENERATOR", // JPA 에서 사용하는 시퀀스 이름(DB의 시퀀스 이름이 아님!)
		sequenceName = "INBOUND_ITEM_SEQ", // 오라클에서 사용하는 시퀀스 이름
		initialValue = 1, 			// 초기값(오라클 시퀀스의 start with 1과 동일)
		allocationSize = 1          // 증가값(오라클 시퀀스의 increment by 값과 동일)
		)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class InboundItem {
	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INBOUND_ITEM_GENERATOR")
	@Column(name = "INBOUND_ITEM_ID", updatable = false)
	private Long InboundItemId; // 입고대기품목 id
	
	@Column(nullable = false)
	private String lotNo; // 로트넘버
	
	@Column(nullable = false)
	private String inboundId; 
	
	@Column(nullable = false)
	private String itemId; // 원자재/부자재, 완제품의 기준정보 고유값
	
	@Column(nullable = false)
	private Long requestAmount; // 발주수량
	
	@Column(nullable = true)
	private Long inboundAmount; // 입고수량
	
	@Column(nullable = true)
	private Long disposeAmount; // 폐기수량
	
	@Column(nullable = false)
	private LocalDateTime manufactureDate; // 제조일
	
	@Column(nullable = true)
	private LocalDateTime expirationDate; // 유통기한
	
	@Column(nullable = false)
	private String itemType; // 아이템타입

	@Column(nullable = true)
	private String locationId; //창고위치
	
	
}
