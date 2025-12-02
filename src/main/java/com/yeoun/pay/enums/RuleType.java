package com.yeoun.pay.enums;

public enum RuleType {
    AMT("금액"),
    RATE("비율"),
    FORMULA("수식");

    private final String label;
    RuleType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
