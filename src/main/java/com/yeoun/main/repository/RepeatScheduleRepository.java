package com.yeoun.main.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.main.entity.RepeatSchedule;
import com.yeoun.main.entity.Schedule;

@Repository
public interface RepeatScheduleRepository extends JpaRepository<RepeatSchedule, Long> {
	

	
}
