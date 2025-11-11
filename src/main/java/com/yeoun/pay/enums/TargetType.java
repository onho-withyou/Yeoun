package com.yeoun.pay.enums;

public enum TargetType {
    ALL("전체"),
    EMP("사원"),
    DEPT("부서"),
    GRADE("직급/등급");

    private final String label;
    TargetType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
