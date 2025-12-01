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
@Table(name = "MATERAIAL_ORDER")
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class MaterialOrder {
	@Id @Column(name = "LOCATION_ID", updatable = false)
	private String orderId; // 로케이션 고유ID
	
	@Column(nullable = false)
	private String clientId; // 거래처ID
	@Column(nullable = false)
	private String status; // 상태
	@Column(nullable = false)
	private String empId; // 담당자
	@Column(nullable = false)
	private String dueDate; // 납기일
	@Column(nullable = false)
	private String totalAmount; // 발주한 총금액
	@CreatedDate
	private LocalDateTime createdDate; // 등록 일시
	
}
