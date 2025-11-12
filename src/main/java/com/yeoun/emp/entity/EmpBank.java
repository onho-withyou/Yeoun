package com.yeoun.emp.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.common.entity.CommonCode;
import com.yeoun.common.entity.FileAttach;

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
@Table(name = "EMP_BANK")
@SequenceGenerator(
		name = "EMP_BANK_SEQ_GENERATOR",
		sequenceName = "EMP_BANK_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class EmpBank {
	
	// 계좌ID
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMP_BANK_SEQ_GENERATOR")
	@Column(name = "BANK_ID")
	private Long bankId;
	
	// 사원번호 (EMP 테이블 FK)
	@Column(name = "EMP_ID", length = 7, nullable = false)
	private String empId;
	
	// 은행코드 (COMMON_CODE.CODE_ID)
	@Column(name = "BANK_CODE", length = 30, nullable = false)
	private String bankCode;
	
	// 계좌번호
	@Column(name = "ACCOUNT_NO", length = 30, nullable = false)
	private String accountNo;
	
	// 예금주
	@Column(name = "HOLDER", length = 30, nullable = false)
	private String holder;
	
	// 통장사본파일ID
	@Column(name = "FILE_ID")
	private Long fileId;
	
	// 등록 일시
	@CreatedDate
	@Column(name = "CREATED_DATE", nullable = false, updatable = false)
	private LocalDateTime createdDate;
	
	// =====================================================================
	
	// EMP 참조 (사원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", referencedColumnName = "EMP_ID", insertable = false, updatable = false)
    private Emp emp;

    // COMMON_CODE 참조 (은행코드)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BANK_CODE", referencedColumnName = "CODE_ID", insertable = false, updatable = false)
    private CommonCode bank;

    // FILE_ATTACH 참조 (통장사본)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FILE_ID", referencedColumnName = "FILE_ID", insertable = false, updatable = false)
    private FileAttach fileAttach;
	

}
