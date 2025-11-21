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

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
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
        action.setStatus("대기");
        
        action.setAppliedYn("N");     // 발령은 처음 생성될 때 무조건 미적용
        action.setAppliedDate(null);  // 적용일자 없음


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
        
        // ==========================
        // 4. 결재선(approver) 생성
        // ==========================
        // 4-1. 신청자 소속 부서 기준으로 approval_form 가져오기
        String formName = "인사발령신청서";
        String deptIdForForm = emp.getDept().getDeptId(); 
        
        ApprovalForm form = approvalFormRepository
                .findByFormNameAndDeptId(formName, deptIdForForm)
                .orElseThrow(() -> new IllegalStateException(
                        "해당 부서의 인사발령 결재양식이 없습니다. deptId=" + deptIdForForm));
        
        // 3-2. approval_doc 엔티티 생성
        ApprovalDoc approvalDoc = new ApprovalDoc();
        approvalDoc.setApprovalTitle(title);							// 문서제목
        approvalDoc.setEmpId(creator.getEmpId());						// 사원번호
        approvalDoc.setCreatedDate(LocalDate.now());					// 생성일자
        approvalDoc.setDocStatus("1차대기");							// 문서상태
        approvalDoc.setFormType("인사발령신청서");						// 양식종류
        approvalDoc.setToDeptId(toDept.getDeptId());					// 발령부서
        approvalDoc.setReason(hrActionRequestDTO.getActionReason());	// 발령사유
        
        // 3-3. 현재 결재자(1차 결재자) 세팅
        if (form.getApprover1() != null) {
            approvalDoc.setApprover(form.getApprover1());
        }
        
        // 3-4. 전자결재 문서 저장
        ApprovalDoc savedDoc = approvalDocRepository.save(approvalDoc);
        Long approvalId = savedDoc.getApprovalId();

        int order = 1;
        
        // ==========================
        // 4-2. 결재선(Approver) 엔티티들 생성
        // ==========================
        
        // APPROVER_1 (1차 결재자)
        if (form.getApprover1() != null) {
            Approver line1 = new Approver();
            line1.setEmpId(form.getApprover1());				// 결재자 사번
            line1.setApprovalId(approvalId);					// 결재문서 ID
            line1.setApprovalStatus(false);      				// 승인 여부 (false = 대기)
            line1.setOrderApprovers(String.valueOf(order++));	// 결재 순서: 1
            line1.setDelegateStatus(null);  					// 대결자 여부
            line1.setViewing("Y");								// 결재문서 목록에 보이도록
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
            line2.setViewing(null);								// 아직 열람 대상 아님
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
            line3.setViewing(null);
            approverRepository.save(line3);
        }

        // ==========================
        // 5. HR_ACTION에 approvalId 연결
        // ==========================
        saved.setApprovalId(approvalId);      // 발령(HR_ACTION) ← 결재문서 ID 매핑

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
    // 인사 발령 자동 적용
    @Transactional
	public void applyScheduledHrActions() {
    	
    	LocalDate today = LocalDate.now();
    	log.info("[발령자동적용] {} 기준 발령 적용 시작", today);
    	
    	// 1. 승인 완료 + 미적용 + 효력일 도달한 발령 리스트 조회
    	List<HrAction> targetList =
    			hrActionRepository.findByStatusAndAppliedYnAndEffectiveDateLessThanEqual(
    				"승인완료",		// 최종 승인 상태
    				"N", 		// 아직 적용 안 됨
    				today		// 효력일이 오늘 이전/오늘
				);
    	
    	if(targetList.isEmpty()) {
    		log.info("[발령자동적용] 적용할 발령 없음");
    		return;
    	}
    	
    	log.info("[발령자동적용] 총 {}건 적용 예정", targetList.size());
    	
    	// 2. 각 발령을 실제 EMP 정보에 반영
    	for (HrAction action : targetList) {
    		
    		Emp emp = action.getEmp();	// 발령 대상 사원
    		
    		// 발령 유형에 따라 분기
    		switch (action.getActionType()) {
    			case "PROMOTION":
    				if (action.getToPosition() != null) {
    					emp.setPosition(action.getToPosition());
    					log.info("[발령적용] 승진 적용 - 사번:{} / 직급:{}",
                                emp.getEmpId(),
                                action.getToPosition().getPosName());
				}
    			break;
    			
    			case "TRANSFER":
    				if (action.getToDept() != null) {
    					emp.setDept(action.getToDept());
    					log.info("[발령적용] 전보 적용 - 사번:{} / 부서:{}",
                                emp.getEmpId(),
                                action.getToDept().getDeptName());
    				}
    				if(action.getToPosition() != null) {
    					emp.setPosition(action.getToPosition());
    					log.info("[발령적용] 전보 직급 변경 - 사번:{} / 직급:{}",
                                emp.getEmpId(),
                                action.getToPosition().getPosName());
    				}
    				break;
    				
				default:
					log.warn("[발령적용] 미지원 발령타입: {} (ACTION_ID={})",
                            action.getActionType(), action.getActionId());
                    continue;
    		}
    		
    		// 3) HR_ACTION 상태 업데이트 (적용여부 / 적용일자 / 상태)
            action.setAppliedYn("Y");          // 이제 적용 완료
            action.setAppliedDate(today);      // 실제 반영된 날짜 기록
            action.setStatus("적용완료");
    		
    	}
    	
    	log.info("[발령자동적용] 발령 {}건 적용 완료", targetList.size());
		
	}

	// ==========================
    // 3. 인사 발령 목록
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
