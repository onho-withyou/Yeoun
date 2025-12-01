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
@Table(name = "MATERIAL_MST")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) 
public class materialMst {

	@Id
	@Column(name="MAT_ID")
	private String matId; //원재료id
	
	@Column(name="MAT_NAME")
	private String matName; //원재료품목명
			
	@Column(name="MAT_TYPE")
	private String matType; //원재료 유형
	
	@Column(name="MAT_UNIT")
	private String matUnit; //단위(용량)
	
	@Column(name="EFFECTIVE_DATE")
	private String effectiveDate; //유효일자
	
	@Column(name="MAT_DESC")
	private String matDesc; //상세설명
	
	@Column(name="CREATE_ID")
	private String createId; //생성자 id
	
	@Column(name="CREATE_DATE")
	private LocalDate createDate; //생성일시
	
	@Column(name="UPDATE_ID")
	private String updateId; //수정자 id
	
	@Column(name="UPDATE_DATE")
	private LocalDate updateDate; //수정일시

}
