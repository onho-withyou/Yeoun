package com.yeoun.orgchart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 조직도
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrgNodeDTO {
	
	// 트리 구성 정보
	private String deptId;			// 부서ID
	private String parentDeptId;	// 상위부서ID (최상위면 NULL)
	
	// 화면 표시 정보
	private String deptName;		// 부서명
	private String posName;			// 직급명
	private String empName;			// 사원명
	
	// 부서 디자인용
	private String className;

}
