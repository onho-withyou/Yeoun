package com.yeoun.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 권한관리 화면 - 시스템 역할 표시용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
	
	private String roleCode;	// 예) ROLE_HR_ADMIN
	private String roleName;	// 예) HR 관리자

}
