package com.yeoun.emp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.EmpRole;

@Repository
public interface EmpRoleRepository extends JpaRepository<EmpRole, Long> {
	List<EmpRole> findByEmp(Emp emp);
}
