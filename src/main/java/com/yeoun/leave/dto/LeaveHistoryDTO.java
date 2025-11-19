package com.yeoun.leave.dto;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.leave.entity.AnnualLeave;
import com.yeoun.leave.entity.AnnualLeaveHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class LeaveHistoryDTO {
	private Long leaveHistId;
	private String emp_id; // 직원 id
	private String emp_name; //직원 이름
	private String dept_id; // 부서 Id
	private Long leave_id; // 연차 Id
	private String leaveType; // 연차 종류 (연차/반차/병가) / (ANNUAL / HALF / SICK)
	private LocalDate startDate; // 사용 시작일
	private LocalDate endDate; // 사용 종료일
	private int usedDays; //  사용 일수
	private String reason; // 연차 사용 이유
	private Long approvalId; // 결재문서 Id
	
	// ---------------------------------
	@Builder
	public LeaveHistoryDTO(Long leaveHistId, String emp_id, String emp_name, String dept_id, Long leave_id,
			String leaveType, LocalDate startDate, LocalDate endDate, int usedDays, String reason, Long approvalId) {
		this.leaveHistId = leaveHistId;
		this.emp_id = emp_id;
		this.emp_name = emp_name;
		this.dept_id = dept_id;
		this.leave_id = leave_id;
		this.leaveType = leaveType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.usedDays = usedDays;
		this.reason = reason;
		this.approvalId = approvalId;
	}
	
	// --------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public AnnualLeaveHistory toEntity() {
		AnnualLeaveHistory leaveHistroy = modelMapper.map(this, AnnualLeaveHistory.class);
		if(this.getEmp_id() != null) {
			Emp emp = new Emp();
			emp.setEmpId(this.getEmp_id().toString());
			emp.setEmpName(this.getEmp_name());
			if(this.getDept_id() != null) {
				Dept dept = new Dept();
				dept.setDeptId(this.getDept_id());
				emp.setDept(dept);
			}
		leaveHistroy.setEmp(emp);
		}
		return leaveHistroy;
	}
	
	// DTO 타입으로 변환
	public static LeaveHistoryDTO fromEntity(AnnualLeaveHistory annualLeaveHistory) {
		LeaveHistoryDTO historyDTO = modelMapper.map(annualLeaveHistory, LeaveHistoryDTO.class);
		
		historyDTO.setEmp_id((annualLeaveHistory.getEmp().getEmpId()));
		historyDTO.setEmp_name(annualLeaveHistory.getEmp().getEmpName());
		historyDTO.setDept_id(annualLeaveHistory.getDept().getDeptId());
		
		return historyDTO; 
	}
}
