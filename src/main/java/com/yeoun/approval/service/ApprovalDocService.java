package com.yeoun.approval.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.yeoun.approval.dto.ApprovalDocDTO;
import com.yeoun.approval.dto.ApprovalDocGridDTO;
import com.yeoun.approval.dto.ApprovalFormDTO;
import com.yeoun.approval.dto.ApproverDTO;
import com.yeoun.approval.entity.ApprovalForm;
import com.yeoun.approval.entity.Approver;
import com.yeoun.approval.entity.ApproverId;
import com.yeoun.approval.mapper.ApprovalDocMapper;
import com.yeoun.approval.mapper.ApprovalFormMapper;
import com.yeoun.approval.entity.ApprovalDoc;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.approval.repository.ApproverRepository;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Position;
import com.yeoun.hr.entity.HrAction;
import com.yeoun.hr.repository.HrActionRepository;
import com.yeoun.leave.service.LeaveService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ApprovalDocService {

	private final ApprovalDocRepository approvalDocRepository;
	private final ApproverRepository approverRepository;
	private final HrActionRepository hrActionRepository;
	private final ApprovalFormMapper approvalFormMapper;
	private final ApprovalDocMapper approvalDocMapper;
	private final LeaveService leaveService;
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	//기안자 명 불러오기
	@Transactional(readOnly = true)
	public List<Emp> getEmp() {
		return approvalDocRepository.findAllMember();
	}
	//기안자명 불러오기
	@Transactional(readOnly = true)
	public List<Object[]> getEmp2() {
		return approvalDocRepository.findAllMember2();
	}
	//직급정보 불러오기
	@Transactional(readOnly = true)
	public List<Position> getPosition(){
		return approvalDocRepository.findPosition();
	}
	//검색 조회
	@Transactional(readOnly = true)
	public  Map<String, List<ApprovalDocGridDTO>> getAllGridsData(String empId,Map<String,Object> searchParams){
		log.info("searchParams 맵 전체 확인: {}", searchParams);
		searchParams.put("empId",empId );
    
		String createDate = (String) searchParams.get("createDate");
    	String finishDate = (String) searchParams.get("finishDate");
    	String empName = (String) searchParams.get("empName");
    	String approvalTitle = (String) searchParams.get("approvalTitle");

		// 날짜 필터 xml 스타일(createDate -> createDateFilter)
    	if (createDate != null && !createDate.trim().isEmpty()) {
    	    searchParams.put("createDateFilter", createDate);
    	}
    	if (finishDate != null && !finishDate.trim().isEmpty()) {
    	    searchParams.put("finishDateFilter", finishDate);
    	}
	
    	// 기안자및 문서양식 필터 통합 xml 스타일
    	String unifiedFilterValue = null;
    	if (empName != null && !empName.trim().isEmpty()) {
    	    unifiedFilterValue = empName;
    	} else if (approvalTitle != null && !approvalTitle.trim().isEmpty()) {
    	    unifiedFilterValue = approvalTitle;
    	}
	
    	if (unifiedFilterValue != null) {
    	    searchParams.put("empNameTitleFilter", unifiedFilterValue);
    	}

		Map<String, List<ApprovalDocGridDTO>> results = new HashMap<>();
		results.put("grid1Data", approvalDocMapper.searchApprovalItems(searchParams));//결재사항
		results.put("grid2Data", approvalDocMapper.searchAllApproval(searchParams));//전체결재
		results.put("grid3Data", approvalDocMapper.searchMyApprovalList(searchParams));//내결재목록
		results.put("grid4Data", approvalDocMapper.searchPendingApproval(searchParams));//결재대기
		results.put("grid5Data", approvalDocMapper.searchCompletedApproval(searchParams));//결재완료
    
		return results;
	}
	
    //결재 문서 등록
    public void saveApprovalDoc(String empId, Map<String,String> doc) {
        log.info(">>>>>>>>>>>>>>>>>> approvalDoc : " + doc);
        ApprovalDoc approvalDoc = new ApprovalDoc();
        ApprovalDocDTO approvalDocDTO = new ApprovalDocDTO();
        doc.forEach((key, value) -> {
            System.out.println(key + " : " + value);
        });

		
      	// 날짜 파싱 
      	LocalDate createdDate = parseDateSafely(doc.get("createdDate"));
		LocalDate finishDate = parseDateSafely(doc.get("finishDate"));
		LocalDate startDate = parseDateSafely(doc.get("startDate"));
		LocalDate endDate = parseDateSafely(doc.get("endDate"));

		log.info(">>>>>>>>>>>>>>>>>> doc.get(\"docStatus\") : " + doc.get("docStatus"));
		approvalDoc.setApprovalId(null);//문서id -자동생성됨
        approvalDoc.setApprovalTitle(doc.get("approvalTitle")); //문서제목
        approvalDoc.setEmpId(empId); //기안자 사번번호
        approvalDoc.setCreatedDate(createdDate);//문서생성일= 결재시작일 =오늘날짜
        approvalDoc.setFinishDate(finishDate);//결재마감일
		approvalDoc.setDocStatus(doc.get("docStatus"));//1차대기 -결재권한자가있을때
        approvalDoc.setFormType(doc.get("drafting"));//양식종류
        approvalDoc.setApprover(doc.get("docApprover"));//결재권한자
        //연차,반차 신청서
		approvalDoc.setStartDate(startDate);// 휴가시작일
        approvalDoc.setEndDate(endDate);//휴가종료일
        approvalDoc.setLeaveType(doc.get("leaveType"));//휴가유형
		//인사발령신청서
		approvalDoc.setToPosCode(doc.get("position"));//직급 - 
        approvalDoc.setToDeptId(doc.get("toDeptId"));//발령부서
		// 지출결의서
        approvalDoc.setExpndType(doc.get("expndType"));//지출타입
        approvalDoc.setReason(doc.get("reason"));//사유
		
        //결재문서
		approvalDocDTO.setApprovalId(approvalDoc.getApprovalId());//결재문서id
        approvalDocDTO.setApprovalTitle(approvalDoc.getApprovalTitle());//문서제목
        approvalDocDTO.setEmpId(approvalDoc.getEmpId());//로그인한 사람 사원번호
        approvalDocDTO.setCreatedDate(approvalDoc.getCreatedDate());//생성일자
        approvalDocDTO.setFinishDate(approvalDoc.getFinishDate());//완료예정일자
        approvalDocDTO.setStartDate(approvalDoc.getStartDate());//시작휴가일자
        approvalDocDTO.setEndDate(approvalDoc.getEndDate());//종료휴가날짜
        approvalDocDTO.setFormType(approvalDoc.getFormType());//양식종류
        approvalDocDTO.setApprover(approvalDoc.getApprover());//결재권한자
        approvalDocDTO.setDocStatus(approvalDoc.getDocStatus());//문서상태
        approvalDocDTO.setLeaveType(approvalDoc.getLeaveType());//연차유형
		approvalDocDTO.setToPosCode(approvalDoc.getToPosCode()); //직급코드
        approvalDocDTO.setExpndType(approvalDoc.getExpndType());//지출종류
        approvalDocDTO.setReason(approvalDoc.getReason());//사유

		approvalDocRepository.save(approvalDoc);

		Long generatedApprovalId = approvalDoc.getApprovalId();
		
        String[] approverKeys = {"approverEmpIdOVD1", "approverEmpIdOVD2", "approverEmpIdOVD3"};
        for (String key : approverKeys) {
            processApprover(generatedApprovalId, doc, key);
        }		

    }

	//기안서 양식종류
	@Transactional(readOnly = true)
	public List<ApprovalForm> getFormTypes(String deptId) {
		return approvalDocRepository.findAllFormTypes(deptId);
	}
	//부서목록조회
	@Transactional(readOnly = true)
	public List<Dept> getDept() {
		return approvalDocRepository.findAllDepartments();
	}
	//그리드 - 1.결재사항 - 진행해야할 결재만 - 결재권한자만 볼수있음
	@Transactional(readOnly = true)	
	public List<Object[]> getPendingApprovalDocs(String empId) {
		return approvalDocRepository.findPendingApprovalDocs(empId);
	}
	//그리드 - 2.전체결재- 나와관련된 모든 결재문서
	@Transactional(readOnly = true)	
	public List<Object[]> getAllApprovalDocs(String empId) {
		return approvalDocRepository.findAllApprovalDocs(empId);
	}
	//그리드 - 3.내결재목록 - 내가 올린결재목록
	@Transactional(readOnly = true)	
	public List<Object[]> getMyApprovalDocs(String empId) {
		return approvalDocRepository.findMyApprovalDocs(empId);
	}
	//그리드 - 4.결재대기 - 나와관련된 모든 결재문서
	@Transactional(readOnly = true)
	public List<Object[]> getWaitingApprovalDocs(String empId) {
		return approvalDocRepository.findWaitingApprovalDocs(empId);
	}	
	//그리드 - 5.결재완료 - 결재권한자가 결재를 완료하면 볼수 있음(1차,2차,3차 모든결재 완료시)
	 @Transactional(readOnly = true)		
	 public List<Object[]> getFinishedApprovalDocs(String empId) {
	 	return approvalDocRepository.findFinishedApprovalDocs(empId);
	 }

	 // --------------------------------------------------------------------------------------
	 // 결재 승인 메서드 
	 @Transactional
	 public void updateApproval(Long approvalId, String empId, String btn) {
		 // 전달받은 문서ID를 사용하여 문서엔티티 가져오기
		 ApprovalDoc approvalDoc = approvalDocRepository.findById(approvalId)
				 					.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 결재문서 입니다."));
		 
		 // 결재승인 버튼을 눌렀을 때
		 if("accept".equals(btn)) {
			 // 해당 문서의 approvalId를 통해 approver 객체에서 승인권자 목록 가져오기
			 List<Approver> approverList = approverRepository.findByApprovalId(approvalId);
			 ApproverId approverId = new ApproverId(approvalDoc.getApprovalId(), approvalDoc.getApprover());
			 
			 
			 // 불러온 승인권자리스트 요소 반복
			 for(Approver approver : approverList) {
				 // 현재 로그인사용자와 승인권자의 empId가 동일한지 하고, 현재 승인권자의 순서가 마지막 순서인지 확인후
				 if(empId.equals(approver.getEmpId())) {
					 // 현재 로그인 사용자와 승인권자의 empId가 동일하고, 현재 승인권자의 순서가 마지막 순서일 때
					 if(Integer.parseInt(approver.getOrderApprovers()) == approverList.size()) {
						 // approvalDoc의 status를 완료로 변경
						 approvalDoc.setDocStatus("완료");
						 
						 // 인사 발령 등록 폼을 통한 신청 (승인 후처리)
						 handleAfterFinalApproval(approvalDoc);
						 
						 // 결재 완료된 문서의 양식이 연차신청서인 경우 동작
						 if ("연차신청서".equals(approvalDoc.getFormType())) {
		                      leaveService.createAnnualLeave(approvalDoc.getApprovalId());
		                   }
						 
					 } else { // 현재 로그인 사용자와 승인권자자의 empId가 동일하고, 최종 결재권자가 아닌경우  
						 // 현재 승인권자 다음 순서 확인( 현재 결재문서, 현재결재순서 + 1)
						 Long nextApproverOrder = Long.parseLong(approver.getOrderApprovers()) + 1; 
						 Approver nextApprover = approverRepository.findByApprovalIdAndOrderApprovers(approver.getApprovalId(), nextApproverOrder.toString());
						 // 다음 결재권자의 VIEWING을 Y로 변경
						 nextApprover.setViewing("Y");
						 
						 // approvalDoc의 approver을 다음 결재권자의 EmpId로 변경
						 approvalDoc.setApprover(nextApprover.getEmpId());
						 // approvalDoc의 status 변경
						 approvalDoc.setDocStatus(nextApprover.getOrderApprovers() + "차 대기");
					 }
				 } 
			 }
		 } else { // 반려 버튼이 눌렸을 때
			 approvalDoc.setDocStatus("반려");
		 }
	 }
	 
	 // 기본 결재권자 가져오기
	 public List<ApprovalFormDTO> getDefaultApproverList(String empId) {
		 
		 List<ApprovalFormDTO> list = approvalFormMapper.findDefaultApproverList(empId);
		 
		 return list;
	 }
	 
	 // ------------------------------------------------------------------------------
	 // 인사 발령 등록 폼을 통한 신청 (승인 후처리)
	private void handleAfterFinalApproval(ApprovalDoc approvalDoc) {
		 
		// 1) 문서가 인사발령 문서인지 확인
		if (!"인사발령신청서".equals(approvalDoc.getFormType())) {
		 return; // 다른 양식이면 종료
		}
		
		// 2) 인사발령 서비스에 해당 결재문서의 발령을 적용
		Long approvalId = approvalDoc.getApprovalId();
		log.info("approvalId 인사발령신청서 id -----> {}",approvalId);
		HrAction hrAction = hrActionRepository.findByApprovalId(approvalId)
                   .orElseThrow(() -> new EntityNotFoundException(
                       // 💡 예외 메시지 구체화
                       "HR 데이터 누락: 결재 최종 승인 후처리용 인사발령(HrAction)을 찾을 수 없습니다. approvalId=" + approvalId));
		
		// 3) 발령 상태만 '승인완료'로 변경 (EMP 적용 금지)
		hrAction.setStatus("승인완료");
		
		// 4) 적용여부는 그대로 'N'
		hrAction.setAppliedYn("N");
		
		// 5) appliedDate NULL
		hrAction.setAppliedDate(null);
	
	}	 
	 
	 // 결제문서 조회시 결제권한자 목록 불러오기
	 public List<ApproverDTO> getApproverDTOList(Long approvalId) {
		 return approverRepository.findByApprovalId(approvalId).stream().map(ApproverDTO::fromEntity).toList();
	 }

	 
	// -------------------------------------------------------------------------------
	// 메인페이지 내가 결제할 결제문서, 내가올린 결제 문서 불러오기
	public Page<ApprovalDocDTO> getSummaryApproval(String empId) {
		
		PageRequest pageRequest = PageRequest.of(0, 5);
		Page<ApprovalDoc> approvalDOCPage = approvalDocRepository.getSummaryApprovalPage(empId, pageRequest);
		
		return approvalDOCPage.map(ApprovalDocDTO::fromEntity); 
	}

	 // 문자열을 LocalDate로 안전하게 파싱하는 헬퍼 메서드
    private LocalDate parseDateSafely(String dateString) {
        if (StringUtils.hasText(dateString)) {
            try {
                return LocalDate.parse(dateString, FORMATTER);
            } catch (Exception e) {
                System.err.println(dateString + " 날짜 파싱 오류 발생: " + e.getMessage());
            }
        }
        return null; 
    }

	private void processApprover(Long approvalId, Map<String, String> doc, String docKey) {
		String approverString = doc.get(docKey); 

		// NullPointerException 방지를 위해 StringUtils.hasText로 확인
		if (StringUtils.hasText(approverString)) {
			String[] parts = approverString.split(","); // 예: ["2506864", "1", "Y"]

			// parts 배열의 길이가 3 이상인지 확인
			if (parts.length >= 3) {
				Approver approver = new Approver();

				approver.setApprovalId(approvalId); // 생성된 문서 ID 설정
				approver.setEmpId(parts[0]);         // 사번 
				approver.setOrderApprovers(parts[1]); // 순서 
				approver.setViewing(parts[2]); // 열람 권한 
				approver.setDelegateStatus(parts[3]); //결재권한자변경여부//전결자 상태
				
				// 기타 필드 설정
				approver.setApprovalStatus(false); // 기본 상태: 미결재
				
				// Approver 엔티티 저장
				approverRepository.save(approver);

			} else {
				log.warn("결재자 데이터 형식이 올바르지 않습니다 ({}): {}", docKey, approverString);
			}
		}
	}
}


