package com.yeoun.pay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.enums.YesNo;

public interface PayItemMstRepository extends JpaRepository<PayItemMst, String> {
		
	List<PayItemMst> findAllByOrderBySortNoAsc();
    List<PayItemMst> findByUseYnOrderBySortNoAsc(YesNo y);
    boolean existsByItemCode(String itemCode);

}
