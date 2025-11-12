package com.yeoun.pay.enums;

public enum CalcStatus {
	SIMULATED, //가계산
	CALCULATED(), //계산완료
	CONFIRMED, //확정
	CANCELLED //취소
}
