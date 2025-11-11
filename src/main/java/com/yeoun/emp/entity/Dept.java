package com.yeoun.emp.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DEPT")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Dept {
	
	// 부서ID
	@Id
	@Column(name = "DEPT_ID", length = 10, nullable = false)
	private String deptId;
	
	// 부서명
	@Column(name = "DEPT_NAME", length = 50, nullable = false)
	private String deptName;
	
	// 상위 부서ID (자기참조)
	@Column(name = "PARENT_ID", length = 10)
	private String parentId;
	
	// 부서장ID
	@Column(name = "HEAD_EMP_ID", length = 7)
	private String headEmpId;
	
	// 부서 약어 (ERP, HR, DEV 등)
    @Column(name = "DEPT_ABBR", length = 20)
    private String deptAbbr;
	
	// 사용여부
	@Column(name = "USE_YN", length = 1, nullable = false)
	private String useYn = "Y";
	
	// 등록일시
	@CreatedDate
	@Column(name = "CREATED_DATE", nullable = false, updatable = false)
	private LocalDateTime createdDate;
	

}
