package com.yeoun.pay.dto;


import java.time.LocalDate;

public interface EmpForPayrollProjection {
	 String getEmpId();
	    String getEmpName();
	    String getRoleCode();
	    String getStatus();
	    LocalDate getHireDate();		
 		String getDeptId();  //부서
}
