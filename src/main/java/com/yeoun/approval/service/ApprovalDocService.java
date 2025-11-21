package com.yeoun.approval.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.approval.dto.ApprovalDocDTO;
import com.yeoun.approval.entity.ApprovalForm;
import com.yeoun.approval.entity.Approver;
import com.yeoun.approval.entity.ApproverId;
import com.yeoun.approval.entity.ApprovalDoc;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.approval.repository.ApproverRepository;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;

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
	//검색 조회
	@Transactional(readOnly = true)
	public List<Object[]> getSearchList(String createDate,String finishDate,String empName,String approvalTitle){
		//return approvalDocRepository.findPendingApprovalDocs(createDate,finishDate,empName,approvalTitle);
		return null;
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
		 
		 // 결제승인 버튼을 눌렀을 때
		 if(btn == "accept") {
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
	 
	// -------------------------------------------------------------------------------
	// 메인페이지 내가 결제할 결제문서, 내가올린 결제 문서 불러오기
	public Page<ApprovalDocDTO> getSummaryApproval(String empId) {
		
		PageRequest pageRequest = PageRequest.of(0, 5);
		Page<ApprovalDoc> approvalDOCPage = approvalDocRepository.getSummaryApprovalPage(empId, pageRequest);
		
		return approvalDOCPage.map(ApprovalDocDTO::fromEntity); 
	}
	

}
