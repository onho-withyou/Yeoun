package com.yeoun.main.entity;


import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.emp.entity.Emp;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "schedule_sharer")
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
// 엔티티의 복합키 설정방법
@IdClass(ScheduleSharerPK.class)
public class ScheduleSharer {
	@Id
	@ManyToOne
	@JoinColumn(name = "SCHEDULE_ID", referencedColumnName = "SCHEDULE_ID", updatable = false, nullable = false)
	Schedule schedule; // 일정ID
	
	@Id
	@ManyToOne
	@JoinColumn(name = "SHARED_EMP_ID", referencedColumnName = "EMP_ID",  updatable = false, nullable = false)
	Emp sharedEmp; // 공유자 아이디

}
