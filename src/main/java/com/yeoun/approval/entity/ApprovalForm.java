package com.yeoun.approval.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="APPROVAL_FORM")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) 
public class ApprovalForm {
    @Id
	@Column(name="FORM_CODE")
	private String formCode; //양식 코드
	
	@Column(name="FORM_NAME")
	private String formName; //양식 이름
	
	@Column(name="DEPT_ID")
	private String deptId; //부서id
	
	@Column(name="APPROVER_1")
	private String approver1; //결재권한자1
	
	@Column(name="APPROVER_2")
	private String approver2; //결재권한자2
	
	@Column(name="APPROVER_3")
	private String approver3; //결재권한자3
}
