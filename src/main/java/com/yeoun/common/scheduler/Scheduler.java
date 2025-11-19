package com.yeoun.common.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yeoun.leave.service.LeaveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class Scheduler {
	
	private final LeaveService leaveService;
	
	// 매년 1월 1일에 연차 초기화
	@Scheduled(cron = "0 0 0 1 1 *")
	public void test() {
		leaveService.updateAllAnnualLeaves();
	}

}
