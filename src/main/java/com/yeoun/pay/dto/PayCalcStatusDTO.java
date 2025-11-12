package com.yeoun.pay.dto;

import java.math.BigDecimal;

public record PayCalcStatusDTO(
        String yyyymm,
        boolean calculated,
        long count,
        BigDecimal totalAmt,
        BigDecimal dedAmt,
        BigDecimal netAmt
) {}
