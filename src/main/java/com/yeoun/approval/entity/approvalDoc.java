package com.yeoun.approval.entity;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="APPROVAL_DOC")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) 
public class approvalDoc {

	@Column(name="APPROVAL_ID")
	private Long approvalId; //결재문서id
	
	@Column(name="APPROVAL_TITLE")
	private String approvalTitle; //문서제목
	
	@Column(name="EMP_ID")
	private String empId; //사원번호
	
	@Column(name="CREATED_DATE")
	private LocalDate createDate; //생성일자 -
	
	@Column(name="FINISH_DATE")
	private LocalDate finishDate; //완료예정일자
	
	@Column(name="DOC_STATUS")
	private String docStatus; //문서상태
	
	@Column(name="FORM_TYPE")
	private String formType; //양식종류
	
	@Column(name="APPROVER")
	private String approvar; //결재권한자
	
	@Column(name="START_DATE")
	private LocalDate startDate; //시작휴가날짜
	
	@Column(name="END_DATE")
	private LocalDate endDate; //종료휴가날짜
	
	@Column(name="TO_DEPT_ID")
	private String toDeptId; //발령부서
	
	@Column(name="EXPND_TYPE")
	private String expndType; //지출종류
	
	@Column(name="REASON")
	private String reason; //사유
}
