package com.yeoun.main.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ScheduleSharerPK implements Serializable {
    private Long schedule;   // Schedule의 PK 타입(Long)
    private String sharedEmp; // Emp의 PK 타입(String 등)
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleSharerPK)) return false;
        ScheduleSharerPK pk = (ScheduleSharerPK) o;
        return Objects.equals(schedule, pk.schedule) &&
               Objects.equals(sharedEmp, pk.sharedEmp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedule, sharedEmp);
    }
}
