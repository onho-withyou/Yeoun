package com.yeoun.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.attendance.entity.AccessLog;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

}
