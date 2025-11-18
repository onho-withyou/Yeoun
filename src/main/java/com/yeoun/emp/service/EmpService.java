package com.yeoun.emp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.auth.repository.RoleRepository;
import com.yeoun.common.repository.CommonCodeRepository;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpDetailDTO;
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
import com.yeoun.messenger.entity.MsgStatus;
import com.yeoun.messenger.repository.MsgStatusRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class EmpService {
	
	private final EmpRepository empRepository;
	private final DeptRepository deptRepository;
	private final PositionRepository positionRepository;
	private final EmpBankRepository empBankRepository;
	private final MsgStatusRepository msgStatusRepository;
	private final BCryptPasswordEncoder encoder;

	// 사원 신규 등록
	@Transactional
	public void registEmp(EmpDTO empDTO) {
		
		// 주민등록번호 중복 검사
		String rrn = empDTO.getRrn();
		if (rrn != null && !rrn.isBlank() && empRepository.existsByRrn(rrn)) {
			throw new IllegalStateException("이미 등록된 주민등록번호입니다.");
		}
		
		// 이메일 / 연락처 중복 검사
	    String email  = empDTO.getEmail();
	    String mobile = empDTO.getMobile();

	    if (email != null && !email.isBlank() && empRepository.existsByEmail(email)) {
	        throw new IllegalStateException("이미 사용 중인 이메일입니다.");
	    }

	    if (mobile != null && !mobile.isBlank() && empRepository.existsByMobile(mobile)) {
	        throw new IllegalStateException("이미 사용 중인 연락처입니다.");
	    }

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
	    int randomImg = ThreadLocalRandom.current().nextInt(1, 6);
	    status.setMsgProfile(randomImg);
	    
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
	// 사원 목록 조회 (검색 + 페이징 포함)
	public Page<EmpListDTO> getEmpList(int page, int size, String keyword, String deptId) {
		
		// 공백 정리
        if (keyword != null) {
            keyword = keyword.trim();
        }
        if (deptId != null && deptId.isBlank()) {
            deptId = null;
        }

		// 정렬 기준은 입사일 최신순 예시
	    Pageable pageable = PageRequest.of(page, size,
	            Sort.by(Sort.Direction.DESC, "hireDate"));
		
		return empRepository.searchEmpList(keyword, deptId, pageable);
	}
	
	// 인사발령 화면에서 쓰는 전체 사원 목록
	public List<EmpListDTO> getEmpListForHrAction(String deptId, String posCode, String keyword) {

		if (keyword != null) {
	        keyword = keyword.trim();
	        if (keyword.isBlank()) keyword = null;
	    }
	    if (deptId != null && deptId.isBlank()) deptId = null;
	    if (posCode != null && posCode.isBlank()) posCode = null;
	    
	    return empRepository.searchForHrActionDto(deptId, posCode, keyword);
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
	public EmpDetailDTO getEmpDetail(String empId) {
		
		Emp emp = empRepository.findById(empId)
	            .orElseThrow(() -> new EntityNotFoundException("사원 없음: " + empId));

		String address = buildAddress(emp);
	    String rrnMasked = maskRrn(emp.getRrn());
	    String bankInfo = buildBankInfo(emp);   // "국민은행 (123456-12-123456)"
	    String photoPath = buildPhotoPath(emp); // null일 수도 있음


	    return new EmpDetailDTO(
	            emp.getEmpId(),
	            emp.getEmpName(),
	            emp.getDept().getDeptName(),
	            emp.getPosition().getPosName(),
	            emp.getGender(),
	            emp.getHireDate() != null ? emp.getHireDate().toString() : "",
	            emp.getMobile(),
	            emp.getEmail(),
	            buildAddress(emp),
	            rrnMasked,
	            bankInfo,
	            photoPath
	    );
	}
	
	// 상세주소 없는 경우 대비
	private String buildAddress(Emp emp) {
	    String addr1 = emp.getAddress1();
	    String addr2 = emp.getAddress2();

	    if (addr2 == null) addr2 = "";

	    return (addr1 + " " + addr2).trim();
	}
	
	private String maskRrn(String rrn) {
	    if (rrn == null || rrn.length() < 6) return "";
	    // 예: 000421-3******
	    return rrn.substring(0, 8) + "******";
	}

	// 급여계좌 문자열 조합
	private String buildBankInfo(Emp emp) {

	    return empBankRepository.findTopByEmpIdOrderByCreatedDateDesc(emp.getEmpId())
	            .map(bank -> {
	            	String bankName = bank.getBank().getCodeName();   // ← 은행명
	            	String account = bank.getAccountNo();             // 계좌번호
	            	String holder  = bank.getHolder();     

	            	// 두 줄 구조로 리턴
	                return bankName + " (" + account + ")\n예금주: " + holder;
	            })
	            .orElse("");
	}

	// 프로필 사진 경로 생성 (추후 수정 예정)
	private String buildPhotoPath(Emp emp) {
	    Long photoFileId = emp.getPhotoFileId(); // Long 타입

	    if (photoFileId == null) {
	        return null; // 사진 없음 → JS에서 기본 이미지 처리
	    }
	    return "/files/photo/" + photoFileId;
	}

	// =============================================================================
	// 사원 정보 수정
	@Transactional(readOnly = true)
	public EmpDTO getEmpForEdit(String empId) {
		
	    Emp emp = empRepository.findById(empId)
	        .orElseThrow(() -> new EntityNotFoundException("사원 없음: " + empId));
	    
	    EmpDTO empDTO = new EmpDTO();
	    
	    // 사원 인사 정보 
	    empDTO.setEmpId(emp.getEmpId());
	    empDTO.setEmpName(emp.getEmpName());
	    empDTO.setGender(emp.getGender());
	    empDTO.setRrn(emp.getRrn());
	    empDTO.setHireDate(emp.getHireDate());
	    empDTO.setStatus(emp.getStatus());
	    empDTO.setEmail(emp.getEmail());
	    empDTO.setMobile(emp.getMobile());
	    empDTO.setPostCode(emp.getPostCode());
	    empDTO.setAddress1(emp.getAddress1());
	    empDTO.setAddress2(emp.getAddress2());
	    empDTO.setDeptId(emp.getDept().getDeptId());
	    empDTO.setPosCode(emp.getPosition().getPosCode());
	    
	    // 사원 급여정보
	    empBankRepository.findByEmpId(empId).ifPresent(bank -> {
	    	empDTO.setBankCode(bank.getBankCode());
	    	empDTO.setAccountNo(bank.getAccountNo());
	    	empDTO.setHolder(bank.getHolder());
    	});
	    
	    return empDTO;
	}

	@Transactional
	public void updateEmp(EmpDTO empDTO) {
		
		Emp emp = empRepository.findById(empDTO.getEmpId())
		            .orElseThrow(() -> new IllegalArgumentException("사원 없음"));

		    // 변경 가능한 필드만 업데이트
		    emp.setEmpName(empDTO.getEmpName());
		    emp.setMobile(empDTO.getMobile());
		    emp.setEmail(empDTO.getEmail());
		    emp.setStatus(empDTO.getStatus());
		    emp.setPostCode(empDTO.getPostCode());
		    emp.setAddress1(empDTO.getAddress1());
		    emp.setAddress2(empDTO.getAddress2());
		    
		    // === 급여계좌 업데이트 ===
		    if (empDTO.getBankCode() != null && empDTO.getAccountNo() != null) {
		        EmpBank empBank = empBankRepository.findByEmpId(emp.getEmpId())
		                .orElseGet(() -> {
		                    EmpBank bank = new EmpBank();
		                    bank.setEmpId(emp.getEmpId());
		                    return bank;
		                });

		        empBank.setBankCode(empDTO.getBankCode());
		        empBank.setAccountNo(empDTO.getAccountNo());
		        empBank.setHolder(empDTO.getHolder());
		        empBank.setFileId(empDTO.getFileId());

		        empBankRepository.save(empBank);
		    }
		    
		    // 추후 사진 추가
		
	}

	



}
