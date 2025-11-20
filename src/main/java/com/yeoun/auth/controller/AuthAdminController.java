package com.yeoun.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.service.AuthAdminService;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.repository.DeptRepository;

import lombok.RequiredArgsConstructor;

// 관리자 - 접근권한 부여
@Controller
@RequestMapping("/auth/manage")
@RequiredArgsConstructor
public class AuthAdminController {
	
	private final AuthAdminService authAdminService;
    private final DeptRepository deptRepository;
	
	// 권한관리 메인 화면
	@GetMapping("")
	public String managePage(Model model) {
		
		model.addAttribute("deptList", deptRepository.findActive());
		model.addAttribute("roleList", authAdminService.getAllRoles());
		
		return "auth/manage";
	}
	
	// 사원 목록 데이터 API
	@GetMapping("/data")
	@ResponseBody
	public Map<String, Object> getEmpData(@RequestParam(required = false, name = "keyword") String keyword,
										  @RequestParam(required = false, name = "deptId") String deptId) {
		
		List<EmpListDTO> list = authAdminService.getEmpListForAuth(deptId, keyword);
		
		Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", 1);
        pagination.put("totalCount", list.size());

        Map<String, Object> data = new HashMap<>();
        data.put("contents", list);
        data.put("pagination", pagination);

        Map<String, Object> result = new HashMap<>();
        result.put("result", true);
        result.put("data", data);
		
		return result;
	}
	
	// 사원 현재 역할 목록
	@GetMapping("/{empId}/roles")
	@ResponseBody
	public List<String> getRoles(@PathVariable("empId") String EmpId) {
		return authAdminService.getRoleCodesByEmp(EmpId);
	}
	
	

} // AuthAdminController 끝




















