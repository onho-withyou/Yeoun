package com.yeoun.order.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.emp.entity.Emp;

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

@Entity
@Table(name = "WORKER_SCHEDULE")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class WorkerSchedule {
	
	// 테이블 식별자
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WS_SEQ")
    @SequenceGenerator(
      name = "WS_SEQ",
      sequenceName = "WS_SEQ",
      allocationSize = 1
    )
	private Long wsId;
	
	// 스케줄
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SCHEDULE_ID", nullable = false)
	private WorkerSchedule schedule;
    
    // 작업자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "WORKER_ID", nullable = false)
    private Emp worker;
    
    // 담당 설비
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EQUIP_ID", nullable = false)
    private ProdEquip equipment;

}




