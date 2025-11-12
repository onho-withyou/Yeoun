package com.yeoun.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

	// 사원 ID 및 현재 날짜 기준으로 출/퇴근 데이터 조회
	Optional<Attendance> findByEmpIdAndWorkDate(String empId, LocalDate now);

	// 출근 기록 있는지 확인
	boolean existsByEmpIdAndWorkDate(String empId, LocalDate today);

	// 개인 출퇴근 기록 조회
	List<Attendance> findByEmpIdAndWorkDateBetween(String empId, LocalDate startDate, LocalDate endDate);

}
