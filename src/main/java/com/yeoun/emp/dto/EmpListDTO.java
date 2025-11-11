package com.yeoun.emp.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class EmpListDTO {
	
	private LocalDate hireDate;
    private String empId;
    private String empName;
    private String deptName; // 현재 부서명
    private String posName;  // 현재 직급명
    private String mobile;
    private String email;

}
