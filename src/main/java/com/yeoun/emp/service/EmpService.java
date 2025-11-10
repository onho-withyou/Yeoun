package com.yeoun.emp.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Employment;
import com.yeoun.emp.entity.Position;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.EmploymentRepository;
import com.yeoun.emp.repository.PositionRepository;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Transactional
public class EmpService {
	
	private final EmpRepository empRepository;
	private final DeptRepository deptRepository;
	private final PositionRepository positionRepository;
	private final EmploymentRepository employmentRepository;
	private final BCryptPasswordEncoder encoder;

	public EmpService(EmpRepository empRepository,
					  DeptRepository deptRepository,
					  PositionRepository positionRepository, 
					  EmploymentRepository employmentRepository,
					  BCryptPasswordEncoder encoder) {
		this.empRepository = empRepository;
		this.deptRepository = deptRepository;
		this.positionRepository = positionRepository;
		this.employmentRepository = employmentRepository;
		this.encoder = encoder;
	}
	
	// ======================================================
	// 사원 등록 요청
	// 1) 사번은 서비스에서 자동 생성 (입사일 기반 + 3자리 난수)
	// 2) 비밀번호 초기값 1234를 BCrypt로 암호화 저장
	public void registEmp(EmpDTO empDTO) {
		
		// 1. 사원번호 자동 생성
		String empId = generateEmpId(empDTO.getHireDate());
		
		// 2. DTO -> Entity 변환
		Emp emp = empDTO.toEntity();
		
		// 3. 서비스에서 세팅해야 하는 값
		emp.setEmpId(empId);					// 자동 사번
		emp.setEmpPwd(encoder.encode("1234"));	// 초기 비밀번호 암호화
		emp.setHireDate(empDTO.getHireDate() != null
						? empDTO.getHireDate()
						: LocalDate.now());		// 기본 입사일
		emp.setStatus(empDTO.getStatus() != null
					  ? empDTO.getStatus()
					  : "ACTIVE");				// 기본 상태
		emp.setRoleCode(empDTO.getRoleCode() != null
						? empDTO.getRoleCode()
						: "ADMIN_USER");		// 기본 권한
		
		// 부서/직급 FK 준비
        Dept dept = deptRepository.findById(empDTO.getDeptId())
                     .orElseThrow(() -> new IllegalArgumentException("부서 없음: " + empDTO.getDeptId()));
        Position pos = positionRepository.findById(empDTO.getPosCode())
                        .orElseThrow(() -> new IllegalArgumentException("직급 없음: " + empDTO.getPosCode()));
		
		// 4. DB 저장
		empRepository.saveAndFlush(emp);
		
		// 5. EMPLOYMENT 입사 이력 저장
		Employment employment = new Employment();
		employment.setEmp(emp);
		employment.setDept(dept);
		employment.setPosition(pos);
		employment.setStartDate(emp.getHireDate());
		employment.setEndDate(null);
		employment.setRemark("입사 등록");
		
		// 6. DB 저장
		employmentRepository.save(employment);
		
		log.info("EMP 저장 + EMPLOYMENT 저장 완료 - empId={}, dept={}, pos={}",
                emp.getEmpId(), dept.getDeptName(), pos.getPosName());
		
	}

	// 사원번호 생성 로직
	private String generateEmpId(LocalDate hireDate) {
		
		LocalDate base = (hireDate != null) ? hireDate : LocalDate.now();
		String datePart = base.format(DateTimeFormatter.ofPattern("yyMM"));
		String randomPart = String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
		
		return datePart + randomPart;
		
	}

	// 사원 목록 조회
	public List<Emp> getEmpList() {
		return empRepository.findAll();
	}

	// 활성화된 부서 목록 조회
	public  List<Dept> getDeptList() {
		return deptRepository.findActive();
	}

	// 활성화된 직급 목록 조회
	public List<Position> getPositionList() {
        return positionRepository.findActive();
    }


}
