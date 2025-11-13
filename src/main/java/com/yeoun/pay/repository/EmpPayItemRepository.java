package com.yeoun.pay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.pay.entity.EmpPayItem;

public interface EmpPayItemRepository extends JpaRepository<EmpPayItem, Long> {

	// 상세조회용 
    List<EmpPayItem> findByPayslipPayslipIdOrderBySortNo(Long payslipId);

    // 삭제용 
    void deleteByPayslipPayslipId(Long payslipId);
}
