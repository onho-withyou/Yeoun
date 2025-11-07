package com.yeoun.pay.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.pay.entity.PayCalcRule;

public interface PayCalcRuleRepository extends JpaRepository<PayCalcRule, Long> {

	List<PayCalcRule> findAllByOrderByPriorityAsc();

	
	

}
