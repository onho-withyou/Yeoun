package com.yeoun.messenger.dto;

import com.yeoun.emp.entity.Emp;
import com.yeoun.messenger.entity.MsgStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 채팅방에 속한 인원 list
public class RoomMemberDTO {
	
	private String empId;
	private String empName;
	private Integer msgProfile;
	private String position;
	private String department;
	
	public static RoomMemberDTO of(Emp emp, MsgStatus status, String posName, String deptName) {
		return RoomMemberDTO.builder()
				.empId(emp.getEmpId())
				.empName(emp.getEmpName())
				.msgProfile(status.getMsgProfile())
				.position(posName)
				.department(deptName)
				.build();
	}

}
