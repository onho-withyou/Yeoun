package com.yeoun.hr.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.approval.entity.ApprovalForm;
import com.yeoun.approval.repository.ApprovalFormRepository;
import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.service.EmpService;
import com.yeoun.hr.dto.HrActionDTO;
import com.yeoun.hr.dto.HrActionPageResponse;
import com.yeoun.hr.dto.HrActionRequestDTO;
import com.yeoun.hr.service.HrActionService;


@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@Log4j2
public class HrActionRestController {
	
	private final HrActionService hrActionService;
	private final EmpService empService;
	private final ApprovalFormRepository approvalFormRepository;
	private final EmpRepository empRepository;
	
	// 인사 발령 목록 (검색 + 페이징)
	@GetMapping("/actions")
	public HrActionPageResponse getHrActionList(@RequestParam(defaultValue = "0", name = "page") int page,
									        	@RequestParam(defaultValue = "10", name = "size") int size,
									        	@RequestParam(defaultValue = "", name = "keyword") String keyword,
									        	@RequestParam(required = false, name = "actionType") String actionType,
									        	@RequestParam(required = false, name = "startDate") String startDate,
									        	@RequestParam(required = false, name = "endDate") String endDate
	) {

	    Page<HrActionDTO> hrPage =
	            hrActionService.getHrActionList(page, size, keyword, actionType, startDate, endDate);

	    return new HrActionPageResponse(
	            hrPage.getContent(),
	            hrPage.getNumber(),
	            hrPage.getSize(),
	            hrPage.getTotalElements(),
	            hrPage.getTotalPages()
	    );
	}

	
	// =============================================================
	// 인사 발령 등록
	@PostMapping("/actions")
	public Long createHrAction(@RequestBody HrActionRequestDTO dto) {
	    return hrActionService.createAction(dto);
	}
	
	// 인사 발령 등록 화면용 사원 목록
	@ResponseBody
	@GetMapping("/employees")
	public List<EmpListDTO> getEmployeesForAction(@RequestParam(required = false, name = "deptId") String deptId,
	        									  @RequestParam(required = false, name = "posCode") String posCode,
	        									  @RequestParam(required = false, name = "keyword") String keyword) {
		return empService.getEmpListForHrAction(deptId, posCode, keyword);
	}
	
	// 결재자 목록 API
	@GetMapping("/approvers")
	public List<EmpListDTO> getApprovers(@RequestParam(name = "formName") String formName) {

	    // 1) 로그인 사용자 정보
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    LoginDTO loginUser = (LoginDTO) auth.getPrincipal();
	    String deptId = loginUser.getDeptId();

	    // 2) 결재양식 조회 (해당 부서 기준)
	    ApprovalForm form = approvalFormRepository
	            .findByFormNameAndDeptId(formName, deptId)
	            .orElseThrow(() -> new IllegalStateException(
	                    "해당 부서 결재 양식 없음: deptId=" + deptId));

	    // 3) 결재자 ID 목록 (null/빈 값 제거)
	    List<String> approverIds = List.of(
	            form.getApprover1(),
	            form.getApprover2(),
	            form.getApprover3()
	    ).stream()
	     .filter(id -> id != null && !id.isBlank())
	     .toList();

	    // 4) 결재자 목록 조회
	    List<Emp> approvers = empRepository.findByEmpIdIn(approverIds);

	    // 5) 엔티티 → DTO 변환
	    return approvers.stream()
	            .map(EmpListDTO::fromEntity)
	            .toList();
	}

}