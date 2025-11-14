package com.yeoun.hr.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Position;
import com.yeoun.approval.entity.approvalDoc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "HR_ACTION")
@SequenceGenerator(
		name = "HR_ACTION_SEQ_GENERATOR",
		sequenceName = "HR_ACTION_SEQ",
		initialValue = 1,
		allocationSize = 1
		)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class HrAction {
	
	// 발령ID
	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "HR_ACTION_SEQ_GENERATOR")
	@Column(name = "ACTION_ID", nullable = false)
	private Long actionId;
	
	// 대상사원 (사원ID) 
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", nullable = false)
    private Emp emp;
	
	// 발령유형
	@Column(name = "ACTION_TYPE", length = 20, nullable = false)
	private String actionType;
	
	// 효력일자
	@Column(name = "EFFECTIVE_DATE")
	private LocalDate effectiveDate;
	
	// 이전부서ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_DEPT_ID")
    private Dept fromDept;
	
	// 이후부서ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_DEPT_ID")
    private Dept toDept;
	
	// 이전직급
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_POS_CODE")
    private Position fromPosition;
	
	// 이후직급
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_POS_CODE")
    private Position toPosition;
	
	// 발령사유
	@Column(name = "REASON", length = 200)
	private String actionReason;
	
	// 상태 (REQ=요청, APPR=승인, REJ=반려 등)
	@Column(name = "STATUS", length = 10, nullable = false)
    private String status = "REQ";   // 자바에서 기본값 세팅
	
	// 등록자 (CREATED_USER = 사원)
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_USER", nullable = false)
    private Emp createdUser;
	
	// 등록일시
	@CreatedDate
	@Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
	
	// 결재문서ID
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPROVAL_ID")
	private approvalDoc approvalDoc;
	 
	
	

}
