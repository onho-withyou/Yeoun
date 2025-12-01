package com.yeoun.masterData.entity;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "QC_ITEM")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) 
public class QcItem {

	@Id
	@Column(name="QC_ITEM_ID")
	private String qcItemId; // QC 항목 id
	
	@Column(name="ITEM_NAME")
	private String itemName; //항목명
	
	@Column(name="TARGET_TYPE")
	private String targetType; //대상구분
	
	@Column(name="UNIT")
	private String unit; //단위
	
	@Column(name="STD_TEXT")
	private String stdText; //기준값텍스트
	
	@Column(name="MIN_VALUE")
	private String minValue; //허용하한값
	
	@Column(name="MAX_VALUE")
	private String maxValue; //허용상한값
	
	@Column(name="USE_YN")
	private String useYn; //사용여부
	
	@Column(name="SORT_ORDER")
	private String sortOrder; //정렬순서
	
	@Column(name="CREATE_ID")
	private String createId; //생성자 id
	
	@Column(name="CREATE_DATE")
	private LocalDate createDate; //생성일시
	
	@Column(name="UPDATE_ID")
	private String updateId; //수정자 id
	
	@Column(name="UPDATE_DATE")
	private LocalDate updateDate; //수정일시
	
}
