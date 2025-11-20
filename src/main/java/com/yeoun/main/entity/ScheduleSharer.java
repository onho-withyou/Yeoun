package com.yeoun.main.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.emp.entity.Emp;
import com.yeoun.main.dto.ScheduleDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
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
