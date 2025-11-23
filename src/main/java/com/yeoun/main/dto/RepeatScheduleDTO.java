package com.yeoun.main.dto;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;

import com.yeoun.emp.entity.Emp;
import com.yeoun.main.entity.RepeatSchedule;
import com.yeoun.main.entity.Schedule;
import com.yeoun.main.entity.ScheduleSharer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RepeatScheduleDTO {
    private Long scheduleId;

    // 반복 주기: "NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    private String recurrenceType;

    // 반복 간격
    private int interval = 1;

    // 반복 요일: ["MO", "WE"] 등
    private List<String> byDay;

    // 반복 날짜 (매월 N일)
    private Integer monthDay;

    // 반복 종료 옵션/종료일
    private String recurrenceEndType; // "none", "until"
    private LocalDate recurrenceUntil;

    // 연 반복(N월 N일)
    private Integer yearMonth;
    private Integer yearDay;

    // === ModelMapper 활용 예 ===
    private static ModelMapper modelMapper = new ModelMapper();

    public RepeatSchedule toEntity() {
        RepeatSchedule repeatSchedule = modelMapper.map(this, RepeatSchedule.class);
//        if(this.getScheduleId() != null) {
//            Schedule schedule = new Schedule();
//            schedule.setScheduleId(this.getScheduleId());
//            repeatSchedule.setSchedule(schedule);
//        }
        return repeatSchedule;
    }

    public static RepeatScheduleDTO fromEntity(RepeatSchedule repeatSchedule) {
        RepeatScheduleDTO dto = modelMapper.map(repeatSchedule, RepeatScheduleDTO.class);
        return dto;
    }

}
