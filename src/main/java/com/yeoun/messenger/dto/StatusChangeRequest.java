package com.yeoun.messenger.dto;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class StatusChangeRequest {
	private String empId;		// 변경 사용자
	private String avlbStat;	// 1차 상태 (온라인/자리비움/다른용무중)	
	private String workStat;	// 2차 상태 (식사 중/회의 중...)	
}
