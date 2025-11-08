package com.yeoun.emp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "EMP")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Emp {

	// 사원번호 (로그인 ID)
	@Id
	@Column(name = "EMP_ID", nullable = false, length = 7)
	private String empId;		
	
	// 비밀번호(BCrypt)
	@Column(name = "EMP_PWD", nullable = false, length = 200)
	private String empPwd;		
	
	// 이름
	@Column(name = "EMP_NAME", nullable = false, length = 50)
	private String empName;		
	
	// 성별 (CHAR(1))
	@Column(name = "GENDER", length = 1)
	private String gender;		
	
	// 주민등록번호 (민감정보, 보관시 암호화/마스킹 고려)
	@Column(name = "RRN", length = 14)
	private String rrn;			
	
	// 연락처
	@Column(name = "MOBILE", length = 20)
	private String mobile;		
	
	// 이메일
	@Column(name = "EMAIL", length = 100)
	private String email;		
	
	// 우편번호
	@Column(name = "POST_CODE", length = 10)
	private String postCode; 	
	
	// 기본주소
	@Column(name = "ADDRESS1", length = 200)
	private String address1; 	
	
	// 상세주소
	@Column(name = "ADDRESS2", length = 200)
	private String address2; 	
	
	// 입사일 (DATE -> LocalDate)
	@Column(name = "HIRE_DATE")
	private LocalDate hireDate;	
	
	// 재직 상태
	@Column(name = "STATUS", nullable = false, length = 10)
	private String status;		
	
	// 사진파일ID (사진 파일 FK)
	@Column(name = "PHOTO_FILE_ID")
	private Long photoFileId;	
	
	// 역할코드
	@Column(name = "ROLE_CODE", length = 20)
	private String roleCode;	
	
	// 등록 일시
	@CreatedDate
	@Column(name = "CREATED_DATE", updatable = false)
	private LocalDateTime createdDate;	// 등록 일시
	
	// 최근 로그인 시각 (로그인 성공 시 서비스에서 수동 업데이트)
	@Column(name = "LAST_LOGIN")
	private LocalDateTime lastLogin;	
	
	
	
	
}
