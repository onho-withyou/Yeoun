package com.yeoun.messenger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RoomLeaveRequest {
	String empId;
	Long roomId;
}
