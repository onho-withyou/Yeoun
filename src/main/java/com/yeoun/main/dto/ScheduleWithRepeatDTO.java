package com.yeoun.main.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleWithRepeatDTO {
    private ScheduleDTO scheduleDTO;
    private RepeatScheduleDTO repeatScheduleDTO; // 혹은 null
    private List<ScheduleSharerDTO> sharedEmpList; // null/빈리스트 가능	
}
