package com.yeoun.masterData.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import oracle.sql.NUMBER;

@Entity
@Table(name = "SAFETY_STOCK")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) 
public class SafetyStock {
	
	@Id
	@Column(name="ITEM_ID", length = 50)
	private String itemId; //품목코드
	
	@Column(name="ITEM_TYPE", length = 10)
	private String itemType; //품목종류
	
	@Column(name="ITEM_NAME", length = 200)
	private String itemName; //품목명
	
	@Column(name="VOLUME")
	private Long volume; //용량
	
	@Column(name="ITEM_UNIT", length = 50)
	private String itemUnit; //단위
	
	@Column(name="POLICY_TYPE")
	private String policyType; //정책방식

	@Column(name="POLICY_DAYS")
	private Long policyDays; //정책일수
	
	@Column(name="DAYS_SAFETY_STOCK_QTY")
	private Long daysSafetyStockQty; //일별 안전재고 수량
	
	@Column(name="SAFETY_STOCK_QTY")
	private Long safetyStockQty; //총 안전재고 수량
	
	@Column(name="STATUS", length = 20)
	private String status; //상태
	
	@Column(name="REMARK", length = 400)
	private String remark; //비고
	
}
