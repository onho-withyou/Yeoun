package com.yeoun.pay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EmpForPayrollProjection {
	 String getEmpId();
	    String getEmpName();
	    String getRoleCode();
	    String getStatus();
	    LocalDate getHireDate();
		BigDecimal getBaseSalary(); //기본급
 		String getDeptId();  //부서
}
