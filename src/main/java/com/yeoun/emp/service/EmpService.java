package com.yeoun.emp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.auth.repository.RoleRepository;
import com.yeoun.common.repository.CommonCodeRepository;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.EmpBank;
import com.yeoun.emp.entity.EmpRole;
import com.yeoun.emp.entity.Position;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpBankRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.EmpRoleRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.messenger.repository.MsgStatus;
import com.yeoun.messenger.repository.MsgStatusRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Transactional
public class EmpService {
	
	private final EmpRepository empRepository;
	private final DeptRepository deptRepository;
	private final PositionRepository positionRepository;
	private final EmpBankRepository empBankRepository;
	private final MsgStatusRepository msgStatusRepository;
	private final BCryptPasswordEncoder encoder;

	public EmpService(EmpRepository empRepository,
					  DeptRepository deptRepository,
					  PositionRepository positionRepository, 
					  RoleRepository roleRepository,
					  EmpRoleRepository empRoleRepository,
					  EmpBankRepository empBankRepository,
					  CommonCodeRepository commonCodeRepository,
					  MsgStatusRepository msgStatusRepository,
					  BCryptPasswordEncoder encoder) {
		this.empRepository = empRepository;
		this.deptRepository = deptRepository;
		this.positionRepository = positionRepository;
		this.empBankRepository = empBankRepository;
		this.msgStatusRepository = msgStatusRepository;
		this.encoder = encoder;
	}
	
	// 사원 신규 등록
	@Transactional
	public void registEmp(EmpDTO empDTO) {

	    // 0) 사번 자동 생성 (충돌 방지 재시도)
	    String empId = generateEmpId(empDTO.getHireDate(), 3);

	    // 1) FK 로드 (부서/직급) - NOT NULL 보장
	    Dept dept = deptRepository.findById(empDTO.getDeptId())
	            .orElseThrow(() -> new IllegalArgumentException("부서 없음: " + empDTO.getDeptId()));
	    Position position = positionRepository.findById(empDTO.getPosCode())
	            .orElseThrow(() -> new IllegalArgumentException("직급 없음: " + empDTO.getPosCode()));

	    // 2) DTO -> Entity
	    Emp emp = empDTO.toEntity();

	    // 3) 서비스에서 세팅해야 하는 값들
	    emp.setEmpId(empId);                               // 자동 사번
	    emp.setEmpPwd(encoder.encode("1234"));             // 초기 비밀번호
	    emp.setHireDate(empDTO.getHireDate() != null ? empDTO.getHireDate() : LocalDate.now());
	    emp.setStatus(empDTO.getStatus() != null ? empDTO.getStatus() : "ACTIVE");
	    emp.setDept(dept);
	    emp.setPosition(position);

	    // 4) EMP 저장
	    empRepository.saveAndFlush(emp);
	    
	    // 5) 메신저 상태(MSG_STATUS) 저장
	    MsgStatus status = new MsgStatus();
	    
	    status.setEmpId(empId);
	    status.setAvlbStat("ONLINE");
	    status.setAvlbUpdated(LocalDateTime.now());
	    status.setAutoWorkStat("IN");
	    status.setWorkStatUpdated(LocalDateTime.now());
	    status.setWorkStatSource("AUTO");
	    status.setOnlineYn("N");
	    
	    msgStatusRepository.save(status);

	    // 6) 급여계좌(EMP_BANK) 저장 (선택값 없으면 스킵)
	    if (empDTO.getBankCode() != null && empDTO.getAccountNo() != null) {
	        EmpBank bank = new EmpBank();
	        bank.setEmpId(emp.getEmpId());
	        bank.setBankCode(empDTO.getBankCode());
	        bank.setAccountNo(empDTO.getAccountNo());
	        bank.setHolder(empDTO.getHolder());
	        bank.setFileId(empDTO.getFileId());
	        empBankRepository.save(bank);
	    }

	    log.info("EMP 등록 완료: empId={}, dept={}, pos={}",
	            emp.getEmpId(), dept.getDeptName(), position.getPosName());
	}

	// 사원번호 생성 로직
	private String generateEmpId(LocalDate hireDate, int maxRetry) {
		LocalDate base = (hireDate != null) ? hireDate : LocalDate.now();
        String datePart = base.format(DateTimeFormatter.ofPattern("yyMM"));
        
        for (int i = 0; i < maxRetry; i++) {
            String randomPart = String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
            String candidate = datePart + randomPart;
            boolean exists = empRepository.existsByEmpId(candidate);
            if (!exists) return candidate;
        }
        throw new IllegalStateException("사번 생성 충돌: 재시도 초과");
	}
	
	// 활성화된 부서 목록 조회
	public  List<Dept> getDeptList() {
		return deptRepository.findActive();
	}

	// 활성화된 직급 목록 조회
	public List<Position> getPositionList() {
        return positionRepository.findActive();
    }
	
	// =============================================================================
	// 사원 목록 조회
	public List<EmpListDTO> getEmpList() {
		log.info("▶ 사원 목록 조회 시작");
		List<Emp> empList = empRepository.findAll();
		
		return empRepository.findAllForList();
	}
	
	// ==============================================================================
	// 사원 정보 조회
	@Transactional(readOnly = true)
	public EmpDTO getEmp(String empId) {
		// EmpRepository - findByempId() 메서드 호출하여 사원 정보 조회
		Emp emp = empRepository.findByEmpId(empId)
				.orElseThrow(() -> new UsernameNotFoundException(empId + " 에 해당하는 사원이 없습니다!"));
		
		for(EmpRole er : emp.getEmpRoles()) {
			String code = (er.getRole() != null) ? er.getRole().getRoleCode() : "NULL";
	        log.info(">>> Role: {}", code);
		}
		// --------------------------------------------------------------------------
		// Emp 엔티티 -> EmpDTO 객체로 변환하여 리턴
		return EmpDTO.fromEntity(emp);
	}
	
	// =============================================
	// 사원 정보 상세 조회
//	public EmpDTO getEmpDetail(String empId) {
//		
//		// 사원 엔티티 조회
//		Emp emp = empRepository.findByEmpId(empId)
//					.orElseThrow(() -> new IllegalArgumentException("사원 없음: " + empId));
//		
//		// Emp 엔티티 -> EmpDTO 객체로 변환하여 리턴
//		return EmpDTO.fromEntity(emp);
//	}



}
