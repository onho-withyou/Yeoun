package com.yeoun.hr.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;

import com.yeoun.emp.dto.EmpListDTO;
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
	public List<EmpListDTO> getEmployeesForAction() {
		return empService.getEmpList();
	}
	
	

}
