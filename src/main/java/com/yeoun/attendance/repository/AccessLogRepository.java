package com.yeoun.attendance.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.yeoun.attendance.entity.AccessLog;

import org.springframework.data.repository.query.Param;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

	// 오늘 날짜의 외근 기록 있는지 조회
	Optional<AccessLog> findByEmpIdAndAccessDate(String empId, LocalDate today);

	// 가장 퇴근 로그 조회
	@Query("""
		    SELECT a
		      FROM AccessLog a
		     WHERE a.empId = :empId
		       AND a.accessDate = :today
		       AND a.returnTime IS NULL
		     ORDER BY a.outTime DESC
		""")
	Optional<AccessLog> findLatestOutWithoutReturn(@Param("empId") String empId, @Param("today") LocalDate today);

}
