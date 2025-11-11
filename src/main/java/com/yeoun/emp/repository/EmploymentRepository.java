package com.yeoun.emp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Employment;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {

	List<Employment> findByEmpInAndEndDateIsNull(List<Emp> emps);
	
	Optional<Employment> findByEmp(Emp Emp);
}
