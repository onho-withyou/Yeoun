package com.yeoun.emp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Table(name = "EMPLOYMENT")
@SequenceGenerator(
		name = "EMPLOYMENT_SEQ_GENERATOR",
		sequenceName = "EMPLOYMENT_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Employment {
	
	// 배치ID
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMPLOYMENT_SEQ_GENERATOR")
	@Column(name = "EMPLOY_ID")
	private Long employId;
	
	// 사원ID (FK: EMP.EMP_ID)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EMP_ID", nullable = false, referencedColumnName = "EMP_ID")
    private Emp emp;
	
    // 부서ID (FK: DEPT.DEPT_ID)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DEPT_ID", nullable = false)
    private Dept dept;
    
    // 직급코드 (FK: POSITION.POS_CODE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "POS_CODE", nullable = false, referencedColumnName = "POS_CODE")
    private Position position;
    
    // 시작일
    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;
    
    // 종료일
    @Column(name = "END_DATE")
    private LocalDate endDate;
    
    // 비고
    @Column(name = "REMARK", length = 200)
    private String remark;
    
    // 등록 일시
    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;
	
	
	

}
