package com.yeoun.pay.dto;

import java.math.BigDecimal;

public interface EmpPayslipDetailDTO {
	String payYymm();
    String getEmpId();
    String getEmpName();
    String getDeptId();
    String getDeptName();
    String getPosCode();
    String getPosName();
//    String getPayDate(); //지급일

    BigDecimal getBaseAmt();
    BigDecimal getAlwAmt();
    BigDecimal getDedAmt();
    BigDecimal getNetAmt();
    BigDecimal getIncAmt();
    BigDecimal getTotAmt();
    
    String getRemark();    //메모
    String getPayYymm();

    
    
}
