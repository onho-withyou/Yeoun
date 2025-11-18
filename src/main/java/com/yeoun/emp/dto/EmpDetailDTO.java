package com.yeoun.emp.dto;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpBankRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmpDetailDTO {
	
	private String empId;
    private String empName;
    private String deptName;
    private String posName;
    private String gender;
    private String hireDate;   
    private String mobile;
    private String email;
    private String address;
    private String rrnMasked;
    private String bankInfo;
    private String photoPath;
    
}
