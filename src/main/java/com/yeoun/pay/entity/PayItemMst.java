package com.yeoun.pay.entity;

import org.hibernate.annotations.Comment;

import com.yeoun.pay.enums.CalcMethod;
import com.yeoun.pay.enums.ItemGroup;
import com.yeoun.pay.enums.ItemType;
import com.yeoun.pay.enums.YesNo;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "PAY_ITEM_MST")
public class PayItemMst {

    // 1) 항목코드 (PK)
    @Id
    @Column(name = "ITEM_CODE", length = 20, nullable = false)
    @Comment("급여 항목 식별코드")
    private String itemCode;

    // 2) 항목명
    @Column(name = "ITEM_NAME", length = 60, nullable = false)
    @Comment("화면/보고서 표시명")
    private String itemName;

    // 3) 항목유형 (수입/공제 구분) : EARNING / DEDUCTION
    @Enumerated(EnumType.STRING)
    @Column(name = "ITEM_TYPE", length = 20, nullable = false)
    @Comment("수입/공제 구분(EARNING / DEDUCTION)")
    private ItemType itemType;

    // 4) 항목그룹 : ALLOWANCE / INCENTIVE / DEDUCTION
    @Enumerated(EnumType.STRING)
    @Column(name = "ITEM_GROUP", length = 20, nullable = false)
    @Comment("기본급/인센티브/공제액 구분( ALLOWANCE / INCENTIVE / DEDUCTION /TAX)")
    private ItemGroup itemGroup;

    // 5) 과세여부 : Y / N  (기본 Y)
    @Enumerated(EnumType.STRING)
    @Column(name = "TAXABLE", columnDefinition = "CHAR(1)")
    @Comment("과세 대상 구분")
    private YesNo taxable;

    // 6) 계산방식 : FIXED / RATE / FORMULA / EXTERNAL  (기본 FIXED)
    @Enumerated(EnumType.STRING)
    @Column(name = "CALC_METHOD", length = 20, nullable = false)
    @Comment("급여 계산 방식(FIXED / RATE / FORMULA / EXTERNAL)")
    private CalcMethod calcMethod;

    // 7) 정렬순서 (숫자 오름차순)
    @Column(name = "SORT_NO", precision = 4, scale = 0)
    private Integer sortNo;

    // 8) 사용여부 : Y / N  (기본 Y, 미사용시 N)
    @Enumerated(EnumType.STRING)
    @Column(name = "USE_YN", columnDefinition = "CHAR(1)")
    private YesNo useYn;

    // 9) 비고
    @Column(name = "REMARK", length = 200)
    private String remark;

    // 기본값 세팅
    @PrePersist
    public void prePersist() {
        if (this.taxable == null)    this.taxable = YesNo.Y;
        if (this.calcMethod == null) this.calcMethod = CalcMethod.FIXED;
        if (this.useYn == null)      this.useYn = YesNo.Y;
    }
}
