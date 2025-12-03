package com.yeoun.pay.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "EMP_PAY_ITEM")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class EmpPayItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_ID")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_PAY_ID")
    private PayrollPayslip payslip;

    @Column(name = "ITEM_TYPE", length = 20, nullable = false)
    private String itemType; // ALW or DED

    @Column(name = "ITEM_CODE", length = 30)
    private String itemCode;

    @Column(name = "ITEM_NAME", length = 60)
    private String itemName;

    @Column(name = "AMOUNT", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "SORT_NO")
    private Integer sortNo;
}
