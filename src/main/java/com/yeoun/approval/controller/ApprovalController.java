package com.yeoun.approval.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.approval.dto.ApprovalDocDTO;
import com.yeoun.approval.dto.ApprovalFormDTO;
import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.approval.service.ApprovalDocService;
import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.service.EmpService;
import com.yeoun.pay.dto.PayrollHistoryProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
@Log4j2
public class ApprovalController {

	private final ApprovalDocService approvalDocService;
	
	//전자결재 연결페이지
  	@GetMapping("/approval_doc")
  	public String approvalDoc(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		model.addAttribute("empList", approvalDocService.getEmp());//결재- 기안자 목록 불러오기
		model.addAttribute("formTypes",approvalDocService.getFormTypes(loginDTO.getDeptId())); //"DEP001"결재- 기안서 양식종류 불러오기
		model.addAttribute("deptList", approvalDocService.getDept()); //결재- 부서목록 불러오기
		model.addAttribute("approvalDocDTO", new ApprovalDocDTO());//결재문서DTO
		return "approval/approval_doc";
 	}
  
	//날짜,기안자,검색구현
	@GetMapping("/search")
	public List<Object[]> getMethodName(@RequestParam(name="start_date") String start_date
										,@RequestParam(name="end_date") String end_date
										,@RequestParam(name="emp_name") String emp_name
										,@RequestParam(name="approval_title") String approval_title) {
		return approvalDocService.getSearchList(start_date,end_date,emp_name,approval_title);
	}


	//사원목록불러오기 토스트 셀렉트박스
	@ResponseBody
	@GetMapping("/empList")
	public List<Object[]> getDeptList() {
		 return approvalDocService.getEmp2();
	}

	//grid - 1.결재사항 - 진행해야할 결재만 - 결재권한자만 볼수있음
	@ResponseBody
	@GetMapping("/pendingApprovalDocGrid")
	public List<Object[]> getPendingApprovalDocs(@AuthenticationPrincipal LoginDTO loginDTO) {
		return approvalDocService.getPendingApprovalDocs(loginDTO.getEmpId());//"2401300"
	}
	//그리드  - 2.전체결재
	@ResponseBody
	@GetMapping("/approvalDocGrid")
	public List<Object[]> getItemList(@AuthenticationPrincipal LoginDTO loginDTO) {
		return approvalDocService.getAllApprovalDocs(loginDTO.getEmpId());//"2104502"
	}
	//그리드 - 3.내 결재목록
	@ResponseBody
	@GetMapping("/myApprovalDocGrid")
	public List<Object[]> getMyApprovalDocs(@AuthenticationPrincipal LoginDTO loginDTO) {
		return approvalDocService.getMyApprovalDocs(loginDTO.getEmpId());
	}
	//그리드 - 4.결재대기
	@ResponseBody	
	@GetMapping("/waitingApprovalDocGrid")
	public List<Object[]> getWaitingApprovalDocs(@AuthenticationPrincipal LoginDTO loginDTO) {
		return approvalDocService.getWaitingApprovalDocs(loginDTO.getEmpId());//"2104502"
	}
	//그리드 - 5.결재완료
	 @ResponseBody
	 @GetMapping("/finishedApprovalDocGrid")
	 public List<Object[]> getFinishedApprovalDocs(@AuthenticationPrincipal LoginDTO loginDTO) {
	 	return approvalDocService.getFinishedApprovalDocs(loginDTO.getEmpId());//"2505823"
	 }

	
}
