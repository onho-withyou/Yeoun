package com.yeoun.main.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.main.entity.Schedule;
import com.yeoun.main.entity.ScheduleSharer;
import com.yeoun.main.entity.ScheduleSharerPK;

@Repository
public interface ScheduleSharerRepository extends JpaRepository<ScheduleSharer, ScheduleSharerPK> {
	
	
}
