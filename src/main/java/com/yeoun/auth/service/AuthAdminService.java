package com.yeoun.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.auth.dto.RoleDTO;
import com.yeoun.auth.entity.Role;
import com.yeoun.auth.repository.RoleRepository;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.EmpRole;
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

	// 4. 사원 역할 저장
	@Transactional
	public void updateEmpRoles(String empId, List<String> roleCodes) {
		
		Emp emp = empRepository.findById(empId)
		        .orElseThrow(() -> new IllegalArgumentException("사원이 없습니다: " + empId));
		
		// 1) 기존 역할 삭제
		empRoleRepository.deleteByEmp_EmpId(empId);
		
		// 2) 아무것도 선택 안 하면 종료 (모든 역할 제거)
		if (roleCodes == null || roleCodes.isEmpty()) return;
		
		// 3) 새 역할 INSERT
		for (String roleCode : roleCodes) {
			
			Role role = roleRepository.findById(roleCode)
							.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ROLE: " + roleCode));
			
			EmpRole er = new EmpRole();
			er.setEmp(emp);
			er.setRole(role);
			
			empRoleRepository.save(er);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
} // AuthAdminService 끝
