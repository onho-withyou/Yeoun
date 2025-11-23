package com.yeoun.main.entity;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.emp.entity.Emp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "REPEAT_SCHEDULE")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
// 엔티티의 복합키 설정방법
public class RepeatSchedule {
	@Id
    private Long scheduleId; // SCHEDULE 테이블의 PK이자 FK

    // SCHEDULE과 1:1 매핑 (PK=FK 방식, or ManyToOne으로 연결도 가능)
    @OneToOne
    @MapsId
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;
 // [반복 패턴]
    @Column(length = 20, nullable = false)
    private String recurrenceType;   // 반복 타입: "NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"

    @Column(nullable = false)
    private Integer interval = 1;    // 반복 간격: N주/월/년 (기본: 1, UI에서 지정)

    // [반복 종료]
    @Column(length = 20)
    private String recurrenceEndType; // 반복 종료 방식: "none", "until"(특정날짜)
    
    private LocalDate recurrenceUntil; // 반복 종료일 (nullable)

    // [WEEKLY] 반복요일
    @ElementCollection
    @CollectionTable(name = "repeat_schedule_days", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "day")
    private List<String> byDay;      // ["MO", "WE", "FR"] 등 요일명 리스트

    // [MONTHLY] 반복 일자
    private Integer monthDay;        // 매월 N일

    // [YEARLY] 반복 월/일
    private Integer yearMonth;       // 매년 N월
    private Integer yearDay;         // 매년 N월의 N일

}
