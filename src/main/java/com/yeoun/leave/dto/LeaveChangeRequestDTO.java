package com.yeoun.leave.dto;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class LeaveChangeRequestDTO {
	private Long leaveId; // 연차 Id
	private String changeType; // increase 또는 decrease
	private int changeDays; // 변경 일수
	private String reason; // 변경 사유
}
