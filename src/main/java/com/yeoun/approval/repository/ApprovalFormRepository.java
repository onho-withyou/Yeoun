package com.yeoun.approval.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.approval.entity.ApprovalForm;
import com.yeoun.emp.entity.Emp;

public interface ApprovalFormRepository extends JpaRepository<ApprovalForm, String> {

	Optional<ApprovalForm> findByFormNameAndDeptId(String formName, String deptId);

}
