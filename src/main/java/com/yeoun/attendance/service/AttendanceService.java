package com.yeoun.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yeoun.attendance.dto.AccessLogDTO;
import com.yeoun.attendance.dto.AttendanceDTO;
import com.yeoun.attendance.dto.WorkPolicyDTO;
import com.yeoun.attendance.entity.Attendance;
import com.yeoun.attendance.entity.WorkPolicy;
import com.yeoun.attendance.repository.AccessLogRepository;
import com.yeoun.attendance.repository.AttendanceRepository;
import com.yeoun.attendance.repository.WorkPolicyRepository;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.repository.EmpRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AttendanceService {
	private final AttendanceRepository attendanceRepository;
	private final WorkPolicyRepository workPolicyRepository;
	private final AccessLogRepository accessLogRepository;
	private final EmpRepository empRepository;

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
			attendance.setWorkIn(LocalTime.now());
			attendance.setStatusCode("IN");
			attendanceRepository.save(attendance);
			
			return "IN";
		} else {
			// 출근 기록이 있는 경우 퇴근 처리
			Attendance attendance = optionalAttendance.get();
			
			if (attendance.getWorkOut() == null) {
				attendance.setWorkOut(LocalTime.now());
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
	
	// 출/퇴근 수기 등록
	@Transactional
	public void registAttendance(AttendanceDTO attendanceDTO) {
		LocalDate today = LocalDate.now();
		
		// 오늘자 출퇴근 기록 조회
		Optional<Attendance> optionalAttendance  = attendanceRepository.findByEmpIdAndWorkDate(attendanceDTO.getEmpId(), today);
				
		if (optionalAttendance.isEmpty()) {
			// 출근 기록이 없을 경우 엔티티 새로 생성
			Attendance attendance = new Attendance();
			attendance.setEmpId(attendanceDTO.getEmpId());
			attendance.setWorkDate(today);
			attendance.setWorkIn(attendanceDTO.getWorkIn());
			attendance.setWorkOut(attendanceDTO.getWorkOut());
			attendance.setStatusCode(attendanceDTO.getStatusCode());
			
			if (attendance.getWorkIn() != null && attendance.getWorkOut() != null) {
				attendance.setWorkDuration(
						(int) ChronoUnit.MINUTES.between(attendance.getWorkIn(), attendance.getWorkOut()));
			}
			
			attendanceRepository.save(attendance);
		} else if (optionalAttendance.isPresent()) {
			throw new IllegalStateException("이미 오늘 출근 기록이 존재합니다.");
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
			
			// optional이 존재하면 변경된 부분 업데이트
			if (optional.isPresent()) {
				WorkPolicy policy = optional.get();
				policy.changePolicy(workPolicyDTO);
				
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

	// 사원 아이디로 사원 조회
	public EmpDTO getEmp(String empId) {
		return empRepository.findById(empId)
							.map(emp -> EmpDTO.fromEntity(emp))
							.orElseThrow(() -> new NoSuchElementException(empId + "에 해당하는 사원이 없습니다."));
	}

	// 출/퇴근 상세 정보
	public AttendanceDTO getAttendance(Long attendanceId) {
		return attendanceRepository.findById(attendanceId)
								   .map(attendance -> AttendanceDTO.fromEntity(attendance))
								   .orElseThrow(() -> new NoSuchElementException("출/퇴근 정보가 없습니다."));
	}

	@Transactional
	// 출퇴근 수정
	public void modifyAttendance(Long attendanceId, AttendanceDTO attendanceDTO) {
		Attendance attendance = attendanceRepository.findById(attendanceId)
				.orElseThrow(() -> new NoSuchElementException("해당 출퇴근 기록을 찾을 수 없습니다."));
		
		attendance.setWorkIn(attendanceDTO.getWorkIn());
		attendance.setWorkOut(attendanceDTO.getWorkOut());
		attendance.setStatusCode(attendanceDTO.getStatusCode());
		attendance.setWorkDuration(
				(int) ChronoUnit.MINUTES.between(attendance.getWorkIn(), attendance.getWorkOut()));
		
	}

	// 외근 등록
	public void registOutwork(AccessLogDTO accessLogDTO) {
		accessLogRepository.save(accessLogDTO.toEntity());
	}

}
