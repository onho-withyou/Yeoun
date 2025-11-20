package com.yeoun.main.dto;

import org.modelmapper.ModelMapper;

import com.yeoun.emp.entity.Emp;
import com.yeoun.main.entity.Schedule;
import com.yeoun.main.entity.ScheduleSharer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleSharerDTO {
	private Long scheduleId; // 일정ID
	
	private String empId;
	
	private String empName;
	
	// ----------------------------------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	public ScheduleSharer toEntity() {
//		ScheduleSharer scheduleSharer = modelMapper.map(this,  ScheduleSharer.class);
		ScheduleSharer scheduleSharer = new ScheduleSharer();
	    
		if(this.getScheduleId() != null) {
			Schedule schedule = new Schedule();
			schedule.setScheduleId(this.getScheduleId());
			
			scheduleSharer.setSchedule(schedule);
		}
		
	    if (this.getEmpId() != null) {
	        Emp emp = new Emp();
	        emp.setEmpId(this.getEmpId());

	        scheduleSharer.setSharedEmp(emp);
	    }
	    
		return scheduleSharer; 
	}
	
	public static ScheduleSharerDTO fromEntity(ScheduleSharer scheduleSharer) {
		ScheduleSharerDTO scheduleSharerDTO = modelMapper.map(scheduleSharer, ScheduleSharerDTO.class);
		scheduleSharerDTO.setScheduleId(scheduleSharer.getSchedule().getScheduleId());
		scheduleSharerDTO.setEmpId(scheduleSharer.getSharedEmp().getEmpId());
		
		return scheduleSharerDTO;
	}
}
