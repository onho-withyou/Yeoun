package com.yeoun.main.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.main.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
	
	@Query("""
			select s
			from Schedule s
			where (s.scheduleStart <= :endDate and s.scheduleFinish >= :startDate)
				and s.scheduleType in ('company', :empId, :myDeptId)
				or s.emp.empId = :empId
			order by s.scheduleStart asc
			""")
	List<Schedule> getIndividualSchedule(@Param("empId")String empId, @Param("myDeptId")String myDeptId
			, @Param("startDate")LocalDateTime startDate, @Param("endDate")LocalDateTime endDate);
	
}
