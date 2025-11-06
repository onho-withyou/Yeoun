package com.yeoun.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.pay.entity.PayItemMst;

public interface PayItemMstRepository extends JpaRepository<PayItemMst, String> {
	
    boolean existsByItemName(String itemName);

}
