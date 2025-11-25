package com.yeoun.common.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yeoun.attendance.service.AttendanceService;
import com.yeoun.hr.service.HrActionService;
import com.yeoun.leave.service.LeaveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class Scheduler {
	
	private final LeaveService leaveService;
	private final HrActionService hrActionService;
	private final AttendanceService attendanceService;
	
	// 매년 1월 1일에 연차 초기화
	@Scheduled(cron = "0 0 0 1 1 *")
	public void test() {
		leaveService.updateAllAnnualLeaves();
	}
	
	// 자동 퇴근 처리
	@Scheduled(cron = "0 5 0 * * *")
	public void autoCloseYesterdayAttendance() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		
		attendanceService.autoCloseAttendance(yesterday);
	}
	
	
	// 발령 자동 적용 스케줄러 (매일 00:00 실행)
	@Scheduled(cron = "0 * * * * *")
	public void applyHrActions() {
		log.info("[스케줄러] 발령 자동 적용 시작");
	
		try {
            hrActionService.applyScheduledHrActions();
            log.info("[스케줄러] 발령 자동 적용 완료");
        } catch (Exception e) {
            log.error("[스케줄러] 발령 자동 적용 중 오류 발생", e);
        }
	
	}
	

}
