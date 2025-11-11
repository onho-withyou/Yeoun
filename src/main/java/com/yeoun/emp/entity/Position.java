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
@Table(name = "POSITION")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Position {
	
	// 직급코드
	@Id
	@Column(name = "POS_CODE", length = 10, nullable = false)
	private String posCode;
	
	// 직급명
	@Column(name = "POS_NAME", length = 30, nullable = false)
	private String posName;
	
	// 서열순서
	@Column(name = "RANK_ORDER", nullable = false)
	private Integer rankOrder;
	
	// 임원 여부 (이사, 사장 등)
    @Column(name = "IS_EXECUTIVE", length = 1, nullable = false)
    private String isExecutive = "N";
	
	// 사용여부
	@Column(name = "USE_YN", length = 1, nullable = false)
    private String useYn = "Y";
	
	// 등록일시
	@CreatedDate
	@Column(name = "CREATED_DATE", nullable = false, updatable = false)
	private LocalDateTime createdDate;
	

}
