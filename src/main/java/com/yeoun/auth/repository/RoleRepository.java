package com.yeoun.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.auth.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
	
	// 활성화된 ROLE 목록
	@Query("SELECT r FROM Role r"
			+ " WHERE r.useYn = 'Y'"
			+ " ORDER BY r.roleName ASC")
	List<Role> findActive();
	
	// 자동 권한 부여용
	Optional<Role> findByRoleCode(String roleCode);

}
