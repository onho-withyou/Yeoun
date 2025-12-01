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
import oracle.sql.NUMBER;

@Entity
@Table(name = "BOM_MST")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BomMst {
	@Id
	@Column(name="BOM_ID", length = 20)
	private NUMBER bomId; //BOMid
	
	@Column(name="PRD_ID", length = 50)
	private String prdId; //제품id
	
	@Column(name="MAT_ID", length = 50)
	private String matId; //원재료id
	
	@Column(name="MAT_QTY")
	private String matQty; //원재료사용량

	@Column(name="MAT_UNIT", length = 20)
	private NUMBER matUnit; //사용단위
	
	@Column(name="BOM_SEQ_NO", length = 10)
	private String bomSeqNo; //순서
	
	@Column(name="CREATED_ID", length = 7)
	private String createdId; //생성자 id
	
	@Column(name="CREATED_DATE")
	private LocalDate createdDate; //생성일시
	
	@Column(name="UPDATED_ID", length = 7)
	private String updatedId; //수정자 id
	
	@Column(name="UPDATED_DATE")
	private LocalDate updatedDate; //수정일시

}
