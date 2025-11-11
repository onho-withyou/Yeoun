package com.yeoun.common.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// 공통 코드 엔터티
@Entity
@Table(name = "COMMON_CODE")
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "codeId")
public class CommonCode {
	
	// 코드 고유ID
	@Id
	@Column(name = "CODE_ID", length = 50, nullable = false)
	private String codeId;

	// 상위 코드 (자기 참조)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_CODE_ID")
	@ToString.Exclude
	private CommonCode parent;
	
	@OneToMany(mappedBy = "parent")
    @OrderBy("codeSeq asc, codeId asc")
	@ToString.Exclude
    private Set<CommonCode> children = new LinkedHashSet<>(); // 하위 코드들
	
	// 코드명
	@Column(name = "CODE_NAME", length = 100, nullable = false)
	private String codeName;
	
	// 코드 순번
	@Column(name = "CODE_SEQ")
	private Integer codeSeq;
	
	// 코드 설명
	@Column(name = "CODE_DESC", length = 100)
	private String codeDesc;
	
	// 계층 깊이
	@Column(name = "LEVEL_NO")
	private Integer levelNo;	
	
	// 사용 여부
	@Column(name = "USE_YN", length = 1)
	private String useYn;
	
	// 시스템 구분
	@Column(name = "SYS_TYPE", length = 30)
	private String sysType;
	
	// 등록자
	@Column(name = "CREATED_USER", length = 7)
	private String createdUser;
	
	// 등록일
	@CreatedDate
	@Column(name = "CREATED_DATE", updatable = false)
	private LocalDateTime createdDate;
	
	// 수정자
	@Column(name = "UPDATED_USER", length = 7)
	private String updatedUser;
	
	// 수정일
	@LastModifiedDate
	@Column(name = "UPDATED_DATE")
	private LocalDateTime updatedDate;
	
}
