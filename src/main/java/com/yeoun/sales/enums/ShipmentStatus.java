package com.yeoun.sales.enums;

public enum ShipmentStatus {
    WAITING,     // 예약대기 (수주 등록 직후)
    RESERVING,   // 예약중 (재고 충분해서 예약됨)
    LACK,        // 재고 부족
    SHIPPED      // 출하완료
}
