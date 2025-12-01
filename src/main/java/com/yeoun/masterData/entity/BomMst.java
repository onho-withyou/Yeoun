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
	@Column(name="BOM_ID")
	private NUMBER bomId; //BOMid
	
	@Column(name="PRD_ID")
	private String prdId; //제품id
	
	@Column(name="MAT_ID")
	private String matId; //원재료id
	
	@Column(name="MAT_QTY")
	private String matQty; //원재료사용량

	@Column(name="MAT_UNIT")
	private String matUnit; //사용단위
	
	@Column(name="BOM_SEQ_NO")
	private String bomSeqNo; //순서
	
	@Column(name="CREATE_ID")
	private String createId; //생성자 id
	
	@Column(name="CREATE_DATE")
	private LocalDate createDate; //생성일시
	
	@Column(name="UPDATE_ID")
	private String updateId; //수정자 id
	
	@Column(name="UPDATE_DATE")
	private LocalDate updateDate; //수정일시

}
