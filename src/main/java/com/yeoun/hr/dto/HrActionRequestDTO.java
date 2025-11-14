package com.yeoun.hr.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HrActionRequestDTO {
	
	// 대상 사원 ID 
	private String empId;         

	// 발령 구분(코드값)
    private String actionType;    
    
 	// 발령 일자
    private LocalDate effectiveDate;  

    // 발령 부서
    private String toDeptId;      
    
    // 발령 직급
    private String toPosCode;    

    // 발령 사유
    private String actionReason; 
    
    private String approverEmpId; // 결재자 사번

}
