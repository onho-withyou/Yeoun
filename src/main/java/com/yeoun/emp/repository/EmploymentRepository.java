package com.yeoun.emp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Employment;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {

}
