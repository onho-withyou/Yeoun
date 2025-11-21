package com.yeoun.messenger.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MSG_STATUS")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert	// null값은 insert문에 포함하지 않는다.
public class MsgStatus {
	
	// 사원 ID (EMP의 EMP_ID FK)
	@Id @Column(nullable = false, length = 7)
	private String empId;
	
	// 가용상태
	@Column(nullable = false, columnDefinition = "VARCHAR2(20) DEFAULT 'ONLINE'")
	private String avlbStat;
	
	// 가용상태 변경일시
	@Column
	private LocalDateTime avlbUpdated;
	
	// 자동 근무상태 (ATTENDANCE의 STATUS_CODE와 동기화)
	@Column(nullable = false, columnDefinition = "VARCHAR2(20) DEFAULT 'IN'")
	private String autoWorkStat;
	
	// 수동 근무상태
	@Column(length = 20)
	private String manualWorkStat;
	
	// 근무상태 변경일시
	@Column(nullable = false)
	private LocalDateTime workStatUpdated;
	
	// 근무상태 변경주체
	@Column(nullable = false, length = 20)
	private String workStatSource;
	
	// 접속 여부
	@Column(nullable = false, length = 1)
	private String onlineYn;
	
	// 마지막 접속시간
	@Column
	private LocalDateTime lastLogin;

	// 프로필 사진
	@Column(nullable = false)
	private Integer msgProfile;
	
	// 비고
	@Column(length = 255)
	private String remark;

}







