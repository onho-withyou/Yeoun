package com.yeoun.attendance.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.attendance.entity.AccessLog;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

	// 오늘 날짜의 외근 기록 있는지 조회
	Optional<AccessLog> findByEmpIdAndAccessDate(String empId, LocalDate today);

	// 가장 퇴근 로그 조회
	AccessLog findTopByEmpIdAndAccessDateAndReturnTimeIsNullOrderByOutTimeDesc(String empId, LocalDate todqy);

}
