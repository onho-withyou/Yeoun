package com.yeoun.pay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.pay.entity.PayItemMst;

public interface PayItemMstRepository extends JpaRepository<PayItemMst, String> {
		
	List<PayItemMst> findAllByOrderBySortNoAsc();
    List<PayItemMst> findByUseYnOrderBySortNoAsc(String useYn);
    boolean existsByItemCode(String itemCode);

}
