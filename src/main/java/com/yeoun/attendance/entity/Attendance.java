package com.yeoun.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yeoun.attendance.dto.AttendanceDTO;
import com.yeoun.emp.entity.Emp;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ATTENDANCE")
@SequenceGenerator(
		name = "ATTENDANCE_SEQ_GENERATOR",
		sequenceName = "ATTENDANCE_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Attendance {
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ATTENDANCE_SEQ_GENERATOR")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "EMP_ID", nullable = false)
	private Emp emp; // 출근한 사원 번호
	
	@Column(nullable = false)
	@CreatedDate
	private LocalDate workDate; // 근무일자
	
	@JsonFormat(pattern = "HH:MM")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime workIn; // 출근시간
	
	@JsonFormat(pattern = "HH:MM")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime workOut; // 퇴근시간
	
	private Integer workDuration; // 근무시간(분단위)
	private String statusCode; // 근태상태
	private String remark; // 비고
	
	@Column(length = 7)
	private String createdUser; // 등록자(수기로 등록 시 사용)
	
	@CreatedDate
	private LocalDateTime createdDate; // 등록일자
	
	@Column(length = 7)
	private String updatedUser; // 수정자
	
	@LastModifiedDate
	private LocalDateTime updatedDate; // 수정일자
	
	// 수기로 출퇴근 등록
	public static Attendance createAttendance(AttendanceDTO attendanceDTO, Emp emp) {
		Attendance attendance = new Attendance();
		
		attendance.emp = emp;
		attendance.workDate = attendanceDTO.getWorkDate();
		attendance.workIn = attendanceDTO.getWorkIn();
		attendance.workOut = attendanceDTO.getWorkOut();
		attendance.statusCode = attendanceDTO.getStatusCode();
		attendance.updateDuration();
		
		return attendance;
	}
	
	// 자동 출근 (출퇴근 버튼 찍었을 경우)
	public static Attendance createForWorkIn(Emp emp, LocalDate date, LocalTime now, 
			LocalTime standardIn, int lateLimit, AccessLog accessLog) {
		Attendance attendance = new Attendance();

		attendance.emp = emp;
		attendance.workDate = date;
		
		// 외근 여부 확인
		if (accessLog != null && "OUTWORK".equalsIgnoreCase(accessLog.getAccessType())) {
			LocalTime outTime = accessLog.getOutTime();
			
			// 외근 시작 시간이 출근 기준 시간 + 지각 유예 시간보다 빠르면 정상 출근 인정
			if (outTime != null && !outTime.isAfter(standardIn.plusMinutes(lateLimit))) {
				attendance.statusCode = "IN";
				attendance.workIn = outTime;
			} else { // 외근 기준 이후면 지각 처리
				attendance.statusCode = "LATE";
				attendance.workIn = (outTime != null) ? outTime : now;
			}
			attendance.remark = accessLog.getReason();
		} else {
			 attendance.statusCode = now.isAfter(standardIn.plusMinutes(lateLimit)) ? "LATE" : "IN";
			 attendance.workIn = now;
		}
		
		 return attendance;
	}
	
	// 퇴근 처리
	public void recordWorkOut(LocalTime now, LocalTime standardOut, AccessLog accessLog) {
		LocalTime  outTime = now;
		
	    if (outTime != null) {
	        this.workOut = outTime;
	        updateDuration();
	    }
	}
	
	// 근무시간 계산
	private void updateDuration() {
		if (this.workIn != null && this.workOut != null) {
			this.workDuration = (int) ChronoUnit.MINUTES.between(this.workIn, this.workOut);
		} else {
			this.workDuration = 0;
		}
	}
	
	// 근태 수정 로직
	public void modifyAttendance(LocalTime workIn, LocalTime workOut, String statusCode) {
		this.workIn = workIn;
		this.workOut = workOut;
		this.statusCode = statusCode;
		updateDuration();
	}
	
	// 퇴근 중복 방지 체크
	public boolean isAlreadyOut() {
		return this.workOut != null;
	}

	// 외근 등록 후 데이터 변경
	public void markAsInByOutwork(LocalTime outTime, WorkPolicy workPolicy, String reason) {
		LocalTime standardIn = LocalTime.parse(workPolicy.getInTime());
		int lateLimit = workPolicy.getLateLimit();
		
		this.statusCode = (outTime.isAfter(standardIn.plusMinutes(lateLimit))) ? "LATE" : "IN";
		this.workIn = outTime;
		this.remark = reason;
	}
}












