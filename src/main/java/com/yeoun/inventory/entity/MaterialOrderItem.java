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
@Table(name = "MATERIAL_ORDER_ITEM")
@SequenceGenerator(
		name = "MATERIAL_ORDER_ITEM_SEQ_GENERATOR", // JPA 에서 사용하는 시퀀스 이름(DB의 시퀀스 이름이 아님!)
		sequenceName = "MATERIAL_ORDER_ITEM_SEQ", // 오라클에서 사용하는 시퀀스 이름
		initialValue = 1, 			// 초기값(오라클 시퀀스의 start with 1과 동일)
		allocationSize = 1          // 증가값(오라클 시퀀스의 increment by 값과 동일)
		)
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class MaterialOrderItem {
	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MATERIAL_ORDER_ITEM_SEQ_GENERATOR") 
	@Column(name = "ORDER_ITEM_ID", updatable = false)
	private String orderItemId; // 발주품목
	
	@Column(nullable = false)
	private String orderId; // 발주ID
	
	@Column(nullable = false)
	private Long itemId; // 발주상품ID
	
	@Column(nullable = false)
	private Long orderAmount; // 발주량
	
	@Column(nullable = false)
	private Long unitPrice; // 단가
	
	@Column(nullable = false)
	private Long supplyAmount; // 공급가액
	
	@Column(nullable = false)
	private Long VAT; // 부가세
	
	@Column(nullable = false)
	private Long totalPrice; // 총금액 
	
}
