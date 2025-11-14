package com.yeoun.attendance.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.attendance.entity.WorkPolicy;

public interface WorkPolicyRepository extends JpaRepository<WorkPolicy, Long> {

	Optional<WorkPolicy> findFirstByOrderByPolicyIdAsc();

}
