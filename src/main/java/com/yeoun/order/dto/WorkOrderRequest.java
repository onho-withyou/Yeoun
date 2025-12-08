package com.yeoun.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class WorkOrderRequest {
    private String planId;
    private String routeId;
    private String prdId;
    private String lineId;
    private Integer planQty;
    private LocalDateTime planStartDate;
    private LocalDateTime planEndDate;
    private String remark;

    // 라인작업자 정보
    private String prcBld;
    private String prcFlt;
    private String prcFil;
    private String prcCap;
    private String prcLbl;
}













