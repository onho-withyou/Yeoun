package com.yeoun.pay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PayslipViewDTO {
    Long getPayslipId();
    String getPayYymm();
    String getEmpId();
    String getEmpName();
    String getDeptId();
    String getDeptName();
    BigDecimal getBaseAmt();
    BigDecimal getAlwAmt();
    BigDecimal getDedAmt();
    BigDecimal getNetAmt();
    BigDecimal getTotAmt();
    String getCalcStatus();
    String confirmUser();
    LocalDateTime confirmDate();

}
