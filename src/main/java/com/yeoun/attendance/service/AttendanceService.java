package com.yeoun.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.attendance.dto.AccessLogDTO;
import com.yeoun.attendance.dto.AttendanceDTO;
import com.yeoun.attendance.dto.WorkPolicyDTO;
import com.yeoun.attendance.entity.AccessLog;
import com.yeoun.attendance.entity.Attendance;
import com.yeoun.attendance.entity.WorkPolicy;
import com.yeoun.attendance.repository.AccessLogRepository;
import com.yeoun.attendance.repository.AttendanceRepository;
import com.yeoun.attendance.repository.WorkPolicyRepository;
import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Emp;
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
		LocalTime now = LocalTime.now();
		
		// 근무정책 조회
		WorkPolicy workPolicy = workPolicyRepository.findFirstByOrderByIdAsc()
				.orElseThrow(() -> new NoSuchElementException("등록된 근무정책이 없습니다."));
		
		// 오늘자 출퇴근 기록 조회
		Attendance attendance  = attendanceRepository.findByEmp_EmpIdAndWorkDate(empId, today).orElse(null);
		// 오늘자 외근 기록 조회
		AccessLog accessLog = accessLogRepository.findByEmpIdAndAccessDate(empId, today).orElse(null);
		
		// 직원 조회
		Emp emp = empRepository.findById(empId)
				.orElseThrow(() -> new NoSuchElementException("사원을 찾을 수 없습니다."));
		
		if (attendance == null) {
			// 출근 기록이 없으면 새로 생성
			Attendance newAttendance = Attendance.createForWorkIn(emp,
                    today,
                    now,
                    LocalTime.parse(workPolicy.getInTime()),
                    workPolicy.getLateLimit(),
                    accessLog);
			 attendanceRepository.save(newAttendance);
			 return newAttendance.getStatusCode();
		}
		
		// 이미 출근 기록이 있으면 퇴근 처리
		if (attendance.isAlreadyOut()) {
			return "ALREADY_OUT";
		}
		
		attendance.recordWorkOut(now, LocalTime.parse(workPolicy.getOutTime()), accessLog);
		
		return "OUT";
	}
	
	// 출/퇴근 수기 등록
	@Transactional
	public void registAttendance(AttendanceDTO attendanceDTO) {
		LocalDate today = LocalDate.now();
		
		// 출근 기록 있는지 확인
		boolean exists = attendanceRepository.existsByEmp_EmpIdAndWorkDate(attendanceDTO.getEmpId(), today);
		
		if (exists) {
			throw new IllegalStateException("이미 오늘 출근 기록이 존재합니다.");
		}
		
		// 직원 조회
		Emp emp = empRepository.findById(attendanceDTO.getEmpId())
				.orElseThrow(() -> new NoSuchElementException("사원을 찾을 수 없습니다."));
		
		attendanceDTO.setWorkDate(today);
		
		Attendance newAttendance = Attendance.createAttendance(attendanceDTO, emp);
		
		attendanceRepository.save(newAttendance);
	}
	
	// 개인 출퇴근 기록
	public List<AttendanceDTO> getMyAttendanceList(String empId, LocalDate startDate, LocalDate endDate) {
		return attendanceRepository.findByEmp_EmpIdAndWorkDateBetween(empId, startDate, endDate)
				.stream()
				.map(AttendanceDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 부서장 또는 관리자가 확인하는 근태현황
	public List<AttendanceDTO> getAttendanceListByRole(LoginDTO loginDTO, LocalDate startDate, LocalDate endDate) {
		String deptId = loginDTO.getDeptId();
		List<String> roles = loginDTO.getAuthorities().stream()
				.map(authority -> authority.getAuthority())
				.collect(Collectors.toList());
		
		List<Attendance> attendanceList = new ArrayList();
		
		if (roles.contains("ROLE_ADMIN")) {
			// 관리자의 경우 전체 직원 조회
			attendanceList = attendanceRepository.findByWorkDateBetween(startDate, endDate);
		} else if (roles.contains("ROLE_ADMIN_SUB")) {
			// 부서장의 본인 부서에 대해서 조회
			attendanceList = attendanceRepository.findByEmp_Dept_DeptIdAndWorkDateBetween(deptId, startDate, endDate);
		}
		
		return attendanceList.stream()
				.map(AttendanceDTO::fromEntity)
				.collect(Collectors.toList());
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
	public EmpListDTO getEmp(String empId) {
		return empRepository.findById(empId)
							.map(emp -> new EmpListDTO(
						            emp.getHireDate(),
						            emp.getEmpId(),
						            emp.getEmpName(),
						            emp.getDept().getDeptName(),
						            emp.getPosition().getPosName(),
						            emp.getPosition().getRankOrder(),
						            emp.getMobile(),
						            emp.getEmail()
						        ))
							.orElseThrow(() -> new NoSuchElementException(empId + "에 해당하는 사원이 없습니다."));
	}

	// 출/퇴근 상세 정보
	public AttendanceDTO getAttendance(Long attendanceId) {
		return attendanceRepository.findById(attendanceId)
								   .map(attendance -> AttendanceDTO.fromEntity(attendance))
								   .orElseThrow(() -> new NoSuchElementException("출/퇴근 정보가 없습니다."));
	}

	// 출퇴근 수정
	@Transactional
	public void modifyAttendance(Long attendanceId, AttendanceDTO attendanceDTO) {
		Attendance attendance = attendanceRepository.findById(attendanceId)
				.orElseThrow(() -> new NoSuchElementException("해당 출퇴근 기록을 찾을 수 없습니다."));
		
		attendance.modifyAttendance(attendanceDTO.getWorkIn(), attendanceDTO.getWorkOut(), attendanceDTO.getStatusCode());
	}

	// 외근 등록
	@Transactional
	public void registOutwork(AccessLogDTO accessLogDTO) {
		String empId = accessLogDTO.getEmpId();
		LocalDate workDate = accessLogDTO.getAccessDate();
		LocalTime outTime= accessLogDTO.getOutTime();
		
		Attendance attendance = attendanceRepository.findByEmp_EmpIdAndWorkDate(empId, workDate)
			    .orElseThrow(() -> new NoSuchElementException("출근 기록이 없습니다."));
	
		WorkPolicy workPolicy = workPolicyRepository.findFirstByOrderByIdAsc()
				.orElseThrow(() -> new NoSuchElementException("근무정책이 없습니다."));
		
		AccessLog accessLog = accessLogRepository.save(accessLogDTO.toEntity());
		
		attendance.markAsInByOutwork(outTime, workPolicy, accessLog.getReason());
	}

}
