package com.yeoun.pay.adapter;


import com.yeoun.pay.repository.EmpNativeRepository;
import com.yeoun.pay.service.PayrollCalcService;
import com.yeoun.pay.service.PayrollCalcService.EmployeeQueryPort;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class EmployeeQueryAdapter implements EmployeeQueryPort {

    private final EmpNativeRepository repo;

    @Override
    public List<PayrollCalcService.SimpleEmp> findActiveEmployees() {

        var rows = repo.findActiveEmpForPayroll();
        if (rows == null || rows.isEmpty()) return List.of();

        return rows.stream()
                .map(r -> new PayrollCalcService.SimpleEmp(
                        r.getEmpId(),
                        r.getDeptId(),
                        r.getHireDate()
                ))
                .toList();
    }


    /** 이름 조회, 부서 조회 */
    @Override
    public String getEmpName(String empId) {
        return repo.findEmpNameById(empId)
                .orElse(null);
    }
 
    @Override
    public String getDeptName(String empId) {
        return repo.findDeptNameById(empId)
                   .orElse(null);
    }
    
    /** 직급 조회 */
    @Override
    public String getEmpPosition(String empId) {
        return repo.findEmpPositionById(empId)
                .orElse(null);
    }

    /** 🔥 사용 연차 조회 */
    @Override
    public int getUsedAnnual(String empId) {
        return repo.findUsedAnnualById(empId)
                .orElse(0);
    }

    @Override
    public int getAnnualRemainForYear(String empId, int year) {
        return repo.findRemainDaysByYear(empId, year).orElse(0);
    }


}
