package com.yeoun.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
import com.yeoun.leave.entity.AnnualLeave;
import com.yeoun.leave.repository.LeaveHistoryRepository;
import com.yeoun.leave.repository.LeaveRepository;
import com.yeoun.leave.service.LeaveService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bytebuddy.description.annotation.AnnotationDescription.Latent;

@Service
@RequiredArgsConstructor
@Log4j2
public class AttendanceService {
	private final AttendanceRepository attendanceRepository;
	private final WorkPolicyRepository workPolicyRepository;
	private final AccessLogRepository accessLogRepository;
	private final LeaveService leaveService;
	private final LeaveHistoryRepository leaveHistoryRepository;
	private final EmpRepository empRepository;

	// 출/퇴근 등록
	@Transactional
	public String registAttendance(String empId) {
		LocalDate today = LocalDate.now();
		LocalTime now = LocalTime.now();
		
		// 근무정책 조회
		WorkPolicy workPolicy = workPolicyRepository.findFirstByOrderByPolicyIdAsc()
				.orElseThrow(() -> new NoSuchElementException("등록된 근무정책이 없습니다."));
		
		// 직원 조회
		Emp emp = empRepository.findById(empId)
				.orElseThrow(() -> new NoSuchElementException("사원을 찾을 수 없습니다."));
		
		// 오늘자 출퇴근 기록 조회
		Attendance attendance  = attendanceRepository.findByEmp_EmpIdAndWorkDate(empId, today).orElse(null);
		
		// 오늘자 외근 기록 조회
		List<AccessLog> accessLogs = accessLogRepository.findByEmpIdAndAccessDate(empId, today);
		
		LocalTime lunchStart = LocalTime.parse(workPolicy.getLunchIn());
		LocalTime lunchEnd = LocalTime.parse(workPolicy.getLunchOut());
		LocalTime standardIn  = LocalTime.parse(workPolicy.getInTime());
		LocalTime standardOut  = LocalTime.parse(workPolicy.getOutTime());
		
		// 점심시간에는 출입 기록 하지 않음
		if (now.isAfter(lunchStart) && now.isBefore(lunchEnd)) {
			return "LUNCH_TIME";
		}
		
		// 오늘 날짜 기준으로 휴무인지 확인
		boolean isHoliday = leaveHistoryRepository.existsByEmp_EmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(empId, today, today);
		
		// 휴무라면 출퇴근이 아니라 외출 현황 로그에만 작성
		if (isHoliday) {
			return processAccessLog(empId, now, today);
		}
		
		// 퇴근 처리 안했을 경우 자동으로 퇴근 처리
		autoCloseYesterdayWork(empId, today, workPolicy);
		
		// 출근 기록이 있고 이미 퇴근을 완료한 경우
		if (attendance != null && attendance.getWorkOut() != null) {
			return processAccessLog(empId, now, today);
		}
		
		// 출근 기록이 없으면 새로 생성
		if (attendance == null) {
			// 기준 출근 시간 이전일 경우 정상 출근
			if (now.isBefore(standardIn)) {
				Attendance newAttendance = Attendance.createForWorkIn(
						emp, today, now, standardIn, workPolicy.getLateLimit(), accessLogs);
				
				attendanceRepository.save(newAttendance);
				
				return newAttendance.getStatusCode();
			}
			// 기준 출근과 퇴근 사이이면 외출로 처리
			if (!now.isAfter(standardOut)) {
				return recordAccessOut(empId, today, now);
			}
			
			// 기준 퇴근 이후일 경우 외출로 처리
			return processAccessLog(empId, now, today);
		}
		// 출근 기록이 있을 경우 
		if (now.isAfter(standardOut)) {
//			AccessLog log = AccessLog.builder()
//					.empId(empId)
//					.accessDate(today)
//					.returnTime(now)
//					.accessType("IN")
//					.build();
			
//			accessLogRepository.save(log);
			
			attendance.recordWorkOut(now, null);
			return "WORK_OUT";
		}
		
		// 근무시간 중 퇴근 처리
//		if (now.isAfter(standardOut) && !attendance.isAlreadyOut()) {
//			attendance.recordWorkOut(now, standardOut);
//			return "WORK_OUT";
//		}
		
		return processAccessLog(empId, now, today);
	}
	
	// 외출 복귀 처리
	private String processAccessLog(String empId, LocalTime now, LocalDate today) {
		List<AccessLog> logs = accessLogRepository.findOutLogsWithoutReturn(empId, today);
		
		// 가장 최신의access_type인 OUT 찾기
		AccessLog lastLog = logs.stream()
				.max(Comparator.comparing(AccessLog::getOutTime))
				.orElse(null);
		
		if (lastLog != null) {
			// OUT -> IN 복귀 처리
			if ("OUT".equalsIgnoreCase(lastLog.getAccessType())) {
				lastLog.accessIn(now, "IN");
				return "IN";
			} else {
				// IN -> OUT 외출 처리
				lastLog.accessOut(now, "OUT");
				return "OUT";
			}
		}
		
		return recordAccessOut(empId, today, now);
	}
	
	// 외출 기록 생성
	private String recordAccessOut(String empId, LocalDate today, LocalTime now) {
		AccessLog newOutLog = AccessLog.builder()
				.empId(empId)
				.accessDate(today)
				.outTime(now)
				.returnTime(null)
				.accessType("OUT")
				.build();
		
		accessLogRepository.save(newOutLog);
		
		return "OUT";
	}
	
	// 퇴근 처리 안했을 경우 자동 퇴근 처리
	@Transactional
	private void autoCloseYesterdayWork(String empId, LocalDate today, WorkPolicy policy) {
		LocalDate yesterday = today.minusDays(1);
		
		Attendance attendance = attendanceRepository.findByEmp_EmpIdAndWorkDate(empId, yesterday)
				.orElse(null);
		
		if (attendance != null && attendance.getWorkOut() == null) {
			// 정책 시간으로 자동 퇴근
			LocalTime autoWorkOut = LocalTime.parse(policy.getOutTime());
			log.info(">>>>>>>>>>>>>> autoWorkOut" + autoWorkOut);
			attendance.recordWorkOut(autoWorkOut, autoWorkOut);
		}
	}
	
	// 출퇴근 수기 등록
	@Transactional
	public void registAttendance(AttendanceDTO attendanceDTO, LoginDTO loginDTO) {
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
		attendanceDTO.setCreatedUser(loginDTO.getEmpId());
		
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
		return  workPolicyRepository.findFirstByOrderByPolicyIdAsc()
				.map(workPolicy -> WorkPolicyDTO.fromEntity(workPolicy))
				.orElseGet(() -> new WorkPolicyDTO());
	}

	// 근무정책 등록 (최초 1회 생성, 그 후로는 업데이트로 진행)
	@Transactional
	public String registWorkPolicy(WorkPolicyDTO workPolicyDTO) {
		try {
			// id를 오름차순으로 정렬해서 첫 번째 row 하나 가져오기
			Optional<WorkPolicy> optional = workPolicyRepository.findFirstByOrderByPolicyIdAsc();
			
			// optional이 존재하면 변경된 부분 업데이트
			if (optional.isPresent()) {
				WorkPolicy policy = optional.get();
				
				String oldBasis = policy.getAnnualBasis(); // 변경되기 전 연차기준
				
				policy.changePolicy(workPolicyDTO);
				
				// 연차 기준 변경 확인
				Boolean isAnnualBasisChanged = !Objects.equals(oldBasis, policy.getAnnualBasis());
				
				if (isAnnualBasisChanged) {
					leaveService.recalculateAllAnnualLeaves(policy);
				}
				
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
				 			.map(EmpListDTO::fromEntity)
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
	public void modifyAttendance(Long attendanceId, AttendanceDTO attendanceDTO, LoginDTO loginDTO) {
		Attendance attendance = attendanceRepository.findById(attendanceId)
				.orElseThrow(() -> new NoSuchElementException("해당 출퇴근 기록을 찾을 수 없습니다."));
		
		LocalTime workIn = attendanceDTO.getWorkIn();
		LocalTime workOut = attendanceDTO.getWorkOut();
		String statusCode = attendanceDTO.getStatusCode();
		String updateUserEmpId = loginDTO.getEmpId();
		
		attendance.modifyAttendance(workIn, workOut, statusCode, updateUserEmpId);
	}

	// 외근 등록
	@Transactional
	public void registOutwork(AccessLogDTO accessLogDTO) {
		String empId = accessLogDTO.getEmpId();
		LocalDate workDate = accessLogDTO.getAccessDate();
		LocalTime outTime= accessLogDTO.getOutTime();
		
		Attendance attendance = attendanceRepository.findByEmp_EmpIdAndWorkDate(empId, workDate)
			    .orElseThrow(() -> new NoSuchElementException("출근 기록이 없습니다."));
	
		WorkPolicy workPolicy = workPolicyRepository.findFirstByOrderByPolicyIdAsc()
				.orElseThrow(() -> new NoSuchElementException("근무정책이 없습니다."));
		
		AccessLog accessLog = accessLogRepository.save(accessLogDTO.toEntity());
		
		attendance.markAsInByOutwork(outTime, workPolicy, accessLog.getReason());
	}

	// 출근 버튼 활성/비활성화 체크하기 위한 로직
	public Object isAttendanceButtonEnabled(String empId) {
		LocalDate today = LocalDate.now();
		
		Attendance attendance = attendanceRepository.findByEmp_EmpIdAndWorkDate(empId, today)
				.orElse(null);
		
		// 출근 기억이 없을 경우 출근 버튼 활성화
		if (attendance == null) {
			return true;
		}
		
		// 퇴근(상태가 WORKOUT)인 경우 출근이 불가능하므로 비활성화
		if (attendance.isAlreadyOut()) {
			return false;
		}
		
		return true;
	}

}
