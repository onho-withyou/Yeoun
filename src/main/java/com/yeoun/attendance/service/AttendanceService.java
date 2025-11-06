package com.yeoun.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yeoun.attendance.entity.Attendance;
import com.yeoun.attendance.repository.AttendanceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AttendanceService {
	private final AttendanceRepository attendanceRepository;

	// 출/퇴근 등록
	public String registAttendance(String empId) {
		LocalDate today = LocalDate.now();
		
		// 오늘자 출퇴근 기록 조회
		Optional<Attendance> optionalAttendance  = attendanceRepository.findByEmpIdAndWorkDate(empId, today);
		
		if (optionalAttendance.isEmpty()) {
			// 출근 기록이 없을 경우 엔티티 새로 생성
			Attendance attendance = new Attendance();
			attendance.setEmpId(empId);
			attendance.setWorkDate(today);
			attendance.setWorkIn(LocalDateTime.now());
			attendance.setStatusCode("IN");
			attendanceRepository.save(attendance);
			
			return "IN";
		} else {
			// 출근 기록이 있는 경우 퇴근 처리
			Attendance attendance = optionalAttendance.get();
			
			if (attendance.getWorkOut() == null) {
				attendance.setWorkOut(LocalDateTime.now());
				attendance.setWorkDuration(
							(int) ChronoUnit.MINUTES.between(attendance.getWorkIn(), attendance.getWorkOut())
				);
				attendanceRepository.save(attendance);
				
				return "OUT";
			} else {
				return "ALREADY_OUT";
			}
		} 
	}

}
