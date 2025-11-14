package com.yeoun.pay.dto;

import java.math.BigDecimal;

public interface PayrollHistoryProjection {

    String getEmpId();
    String getEmpName();
    String getDeptName();
    String getPayYymm();

    BigDecimal getBaseAmt();
    BigDecimal getAlwAmt();
    BigDecimal getDedAmt();
    BigDecimal getTotAmt();
    BigDecimal getNetAmt();

    String getCalcStatus();
}
