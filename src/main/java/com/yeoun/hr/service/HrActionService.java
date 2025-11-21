package com.yeoun.hr.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yeoun.approval.entity.ApprovalDoc;
import com.yeoun.approval.entity.ApprovalForm;
import com.yeoun.approval.entity.Approver;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.approval.repository.ApprovalFormRepository;
import com.yeoun.approval.repository.ApproverRepository;
import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.entity.CommonCode;
import com.yeoun.common.service.CommonCodeService;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Position;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.hr.dto.HrActionDTO;
import com.yeoun.hr.dto.HrActionRequestDTO;
import com.yeoun.hr.entity.HrAction;
import com.yeoun.hr.repository.HrActionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrActionService {

    private final HrActionRepository hrActionRepository;
    private final EmpRepository empRepository;
    private final DeptRepository deptRepository;
    private final PositionRepository positionRepository;
    private final CommonCodeService commonCodeService;
    
    private final ApprovalDocRepository approvalDocRepository;
    private final ApprovalFormRepository approvalFormRepository;
    private final ApproverRepository approverRepository;

    // 1. 발령 등록
    @Transactional
    public Long createAction(HrActionRequestDTO hrActionRequestDTO) {

    	// ==========================
        // 0. 로그인 사용자 정보
        // ==========================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LoginDTO loginUser = (LoginDTO) auth.getPrincipal();
        String loginEmpId = loginUser.getEmpId();

        // 0-1. 등록자 Emp 조회 (신청자)
        Emp creator = empRepository.findById(loginEmpId)
                .orElseThrow(() -> new IllegalArgumentException("등록자 사원을 찾을 수 없습니다. empId=" + loginEmpId));

        // ==========================
        // 1. 발령 대상 및 부서/직급 조회
        // ==========================
        Emp emp = empRepository.findById(hrActionRequestDTO.getEmpId())
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다. empId=" + hrActionRequestDTO.getEmpId()));

        Dept toDept = deptRepository.findById(hrActionRequestDTO.getToDeptId())
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다. deptId=" + hrActionRequestDTO.getToDeptId()));

        Position toPos = positionRepository.findById(hrActionRequestDTO.getToPosCode())
                .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다. posCode=" + hrActionRequestDTO.getToPosCode()));

        // ==========================
        // 2. HR_ACTION 엔티티 생성
        // ==========================
        HrAction action = hrActionRequestDTO.toEntity();	// DTO -> 엔티티 (기본 필드 세팅)

        // 관계 필드 세팅
        action.setEmp(emp);	
        action.setFromDept(emp.getDept());			// 이전 부서
        action.setFromPosition(emp.getPosition());	// 이전 직급
        action.setToDept(toDept); 					// 이후 부서
        action.setToPosition(toPos);				// 이후 직급
        action.setCreatedUser(creator); 			// 등록자
        
        // 발령 상태 기본값 (요청 상태)
        action.setStatus("REQ");

        // 발령(HR_ACTION) 저장
        HrAction saved = hrActionRepository.save(action);
        
        // ==========================
        // 3. 전자결재 문서(approval_doc) 생성
        // ==========================
        // 3-1. 문서 제목 자동 생성
        String actionTypeName = getActionTypeName(hrActionRequestDTO.getActionType()); 
        
        String title = String.format(
    	    "[인사발령/%s] %s %s -> %s (%s)",
    	    actionTypeName,                      // 승진, 전보, 발령 등
    	    emp.getEmpName(),                    // 홍길동
    	    emp.getPosition().getPosName(),      // 대리
    	    toPos.getPosName(),                  // 과장
    	    toDept.getDeptName()                 // 영업팀
    	);
        
        // 3-2. approval_doc 엔티티 생성
        ApprovalDoc approvalDoc = new ApprovalDoc();
        approvalDoc.setApprovalTitle(title);			// 문서제목
        approvalDoc.setEmpId(creator.getEmpId());		// 사원번호
        approvalDoc.setCreatedDate(LocalDate.now());	// 생성일자
        approvalDoc.setDocStatus("1차대기");			// 문서상태
        approvalDoc.setFormType("인사발령신청서");		// 양식종류
        approvalDoc.setToDeptId(toDept.getDeptId());	// 발령부서
        approvalDoc.setReason(hrActionRequestDTO.getActionReason());	// 사유
        
        // 3-3. approval_doc 저장
        ApprovalDoc savedDoc = approvalDocRepository.save(approvalDoc);
        
        // ==========================
        // 4. 결재선(approver) 생성
        // ==========================
        // 신청자 소속 부서 기준으로 approval_form 가져오기
        String formName = "인사발령신청서";
        String deptIdForForm = creator.getDept().getDeptId(); 
        
        ApprovalForm form = approvalFormRepository
                .findByFormNameAndDeptId(formName, deptIdForForm)
                .orElseThrow(() -> new IllegalStateException(
                        "해당 부서의 인사발령 결재양식이 없습니다. deptId=" + deptIdForForm));

        Long approvalId = savedDoc.getApprovalId();

        int order = 1;
        
        // 4-1. approval_doc에 '현재 결재자(1차 결재자)' 세팅
        if (form.getApprover1() != null) {
            savedDoc.setApprover(form.getApprover1());
        }
        
        // APPROVER_1
        if (form.getApprover1() != null) {
            Approver line1 = new Approver();
            line1.setEmpId(form.getApprover1());
            line1.setApprovalId(approvalId);
            line1.setApprovalStatus(false);      // 0 = 대기
            line1.setOrderApprovers(String.valueOf(order++));
            line1.setDelegateStatus(null);   // or "본인"
            line1.setViewing("Y");
            approverRepository.save(line1);
        }

        // APPROVER_2
        if (form.getApprover2() != null) {
            Approver line2 = new Approver();
            line2.setEmpId(form.getApprover2());
            line2.setApprovalId(approvalId);
            line2.setApprovalStatus(false);
            line2.setOrderApprovers(String.valueOf(order++));
            line2.setDelegateStatus(null);
            line2.setViewing("Y");
            approverRepository.save(line2);
        }

        // APPROVER_3
        if (form.getApprover3() != null) {
            Approver line3 = new Approver();
            line3.setEmpId(form.getApprover3());
            line3.setApprovalId(approvalId);
            line3.setApprovalStatus(false);
            line3.setOrderApprovers(String.valueOf(order++));
            line3.setDelegateStatus(null);
            line3.setViewing("Y");
            approverRepository.save(line3);
        }

        // ==========================
        // 5. HR_ACTION에 approvalId 연결
        // ==========================
        saved.setApprovalId(approvalId);      // 필드 타입/이름에 맞게 수정 (Long, String 등)
        // JPA 엔티티니까 트랜잭션 안에서 dirty checking으로 자동 update 되지만,
        // 확실히 하려면 아래처럼 save 한 번 더 호출해도 됨.
        // hrActionRepository.save(saved);
        
        return saved.getActionId();
    }
    
    private String getActionTypeName(String actionType) {

        if (actionType == null || actionType.isBlank()) {
            return "인사발령";
        }

        return switch (actionType) {
            case "PROMOTION"  -> "승진";
            case "TRANSFER"   -> "전보";
            default           -> "인사발령";
        };
    }

	// ==========================
    // 2. 인사 발령 목록
    // ==========================
    public Page<HrActionDTO> getHrActionList(int page, int size, String keyword,
            								 String actionType, String startDate, String endDate) {

		// 0) 검색값 정리
		if (keyword != null) keyword = keyword.trim();
		if (actionType != null && actionType.isBlank()) actionType = null;
		
		LocalDate start = null;
		LocalDate end = null;
		if (startDate != null && !startDate.isBlank()) {
			start = LocalDate.parse(startDate);   // "yyyy-MM-dd" 형식 가정
		}
		if (endDate != null && !endDate.isBlank()) {
			end = LocalDate.parse(endDate);
		}
		
		Pageable pageable = PageRequest.of(page, size,
										   Sort.by(Sort.Direction.DESC, "effectiveDate")  // 발령일자 최신순
		);
		
		// 1) 인사발령 유형 공통코드 → Map
		Map<String, String> actionTypeMap = commonCodeService.getHrActionTypeList()
				.stream()
				.collect(Collectors.toMap(
						 CommonCode::getCodeId,   // ex) PROMOTION, TRANSFER
						 CommonCode::getCodeName  // ex) 승진, 전보
				));
		
		// 2) 조건 + 페이징 걸어서 엔티티 조회
		Page<HrAction> actionPage =
				hrActionRepository.searchHrActions(keyword, actionType, start, end, pageable);
		
		// 3) Page<HrAction> -> Page<HrActionDTO> 매핑
		Page<HrActionDTO> dtoPage = actionPage.map(a -> {
			String typeCode = a.getActionType(); // 예: "PROMOTION"
			String typeName = actionTypeMap.getOrDefault(typeCode, typeCode);
		
			return new HrActionDTO(
						a.getActionId(),
						a.getEmp().getEmpId(),
						a.getEmp().getEmpName(),
						typeCode,
						typeName,
						a.getEffectiveDate(),
						a.getFromDept() != null ? a.getFromDept().getDeptName() : null,
						a.getToDept() != null ? a.getToDept().getDeptName() : null,
						a.getFromPosition() != null ? a.getFromPosition().getPosName() : null,
						a.getToPosition() != null ? a.getToPosition().getPosName() : null,
						a.getStatus(),
						a.getCreatedDate()
					);
			});
		
			return dtoPage;
		}
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
}
