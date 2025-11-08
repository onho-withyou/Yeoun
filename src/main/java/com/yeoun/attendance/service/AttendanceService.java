package com.yeoun.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yeoun.attendance.dto.WorkPolicyDTO;
import com.yeoun.attendance.entity.Attendance;
import com.yeoun.attendance.entity.WorkPolicy;
import com.yeoun.attendance.repository.AttendanceRepository;
import com.yeoun.attendance.repository.WorkPolicyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AttendanceService {
	private final AttendanceRepository attendanceRepository;
	private final WorkPolicyRepository workPolicyRepository;

	// 출/퇴근 등록
	@Transactional
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
	
	// 근무정책 조회
	public WorkPolicyDTO getWorkPolicy() {
		// DB에 정책이 있으면 DTO로 변환하고 없으면 새 DTO 생성
		return  workPolicyRepository.findFirstByOrderByIdAsc()
				.map(workPolicy -> WorkPolicyDTO.fromEntity(workPolicy))
				.orElseGet(() -> new WorkPolicyDTO());
	}

	// 근무정책 등록 (최초 1회 생성, 그 후로는 업데이트로 진행)
	@Transactional
	public String registWorkPolicy(WorkPolicyDTO workPolicyDTO) {
		try {
			// id를 오름차순으로 정렬해서 첫 번째 row 하나 가져오기
			Optional<WorkPolicy> optional = workPolicyRepository.findFirstByOrderByIdAsc();
			
			log.info("＠＠＠＠＠＠＠＠ optional : " + optional);
			// optional이 존재하면 변경된 부분 업데이트
			if (optional.isPresent()) {
				WorkPolicy policy = optional.get();
				policy.setInTime(workPolicyDTO.getInTime());
				policy.setOutTime(workPolicyDTO.getOutTime());
				policy.setLunchIn(workPolicyDTO.getLunchIn());
				policy.setLunchOut(workPolicyDTO.getLunchOut());
				policy.setLateLimit(workPolicyDTO.getLateLimit());
				policy.setAnnualBasis(workPolicyDTO.getAnnualBasis());
				
				return "근무 정책이 수정되었습니다.";
			} else {
				// 최초 1회 생성
				workPolicyRepository.save(workPolicyDTO.toEntity());
				return "근무 정책이 새로 등록되었습니다.";
			}
		} catch (Exception e) {
			return "근무 정책 저장 중 오류가 발생했습니다.";
		}
	}

}
