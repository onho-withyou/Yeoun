package com.yeoun.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.auth.dto.RoleDTO;
import com.yeoun.auth.repository.RoleRepository;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.emp.repository.EmpRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthAdminService {
	
	private final EmpRepository empRepository;
	private final RoleRepository roleRepository;
	private final EmpRoleRepository empRoleRepository;
	
	// 1. 사원 목록 조회
	@Transactional(readOnly = true)
	public List<EmpListDTO> getEmpListForAuth(String deptId, String Keyword) {
		return empRepository.searchActiveEmpList(deptId, null, Keyword);
	}
	
	// 2. 역할 조회
	@Transactional(readOnly = true)
	public List<RoleDTO> getAllRoles() {
		
		return roleRepository.findActive().stream()
				.map(r -> new RoleDTO(r.getRoleCode(), r.getRoleName()))
				.toList();
	}

	
	// 3. 특정 사원의 역할 목록
	@Transactional(readOnly = true)
    public List<String> getRoleCodesByEmp(String empId) {
        return empRoleRepository.findRoleCodesByEmpId(empId);
    }
	
	
	
	
	
	
	
	
	
	
} // AuthAdminService 끝
