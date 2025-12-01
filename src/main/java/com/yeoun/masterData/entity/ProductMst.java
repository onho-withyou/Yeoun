package com.yeoun.masterData.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
import oracle.sql.NUMBER;

@Entity
@Table(name = "PRODUCT_MST")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) 
public class productMst {
		@Id
		@Column(name="PRD_ID")
		private String prdId; //제품id
		
		@Column(name="PRD_NAME")
		private String prdName; //제품명
		
		@Column(name="PRD_CAT")
		private String prdCat; //제품유형
		
		@Column(name="PRD_UNIT")
		private String prdUnit; //단위
		
		@Column(name="PRD_STATUS")
		private String prdStatus; //상태

		@Column(name="EFFECTIVE_DATE")
		private String effectiveDate; //유효일자
		
		@Column(name = "UNIT_PRICE", precision = 18, scale = 2)
		private BigDecimal unitPrice;
		
		@Column(name="PRD_SPEC")
		private String prdSpec; //제품상세설명
		
		@Column(name="CREATE_ID")
		private String createId; //생성자 id
		
		@Column(name="CREATE_DATE")
		private LocalDate createDate; //생성일시
		
		@Column(name="UPDATE_ID")
		private String updateId; //수정자 id
		
		@Column(name="UPDATE_DATE")
		private LocalDate updateDate; //수정일시
		
		

	}