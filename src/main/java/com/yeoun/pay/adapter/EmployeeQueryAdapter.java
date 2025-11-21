package com.yeoun.pay.adapter;

import com.yeoun.pay.repository.EmpNativeRepository;
import com.yeoun.pay.service.PayrollCalcService;
import com.yeoun.pay.service.PayrollCalcService.EmployeeQueryPort;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class EmployeeQueryAdapter implements EmployeeQueryPort {

    private final EmpNativeRepository repo;

    /**
     * ✔ 전체 활성 사원 조회
     *   - 급여 계산 시 "전체 계산"에서 사용
     *   - EmpNativeRepository의 Projection 결과를 SimpleEmp 로 변환
     */
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


    /**
     * 🔥 특정 사원 1명만 조회하는 기능
     *   - 사원별 급여 계산(runMonthlyBatch의 targetEmpId)에서 사용
     *   - DB 조회 후 SimpleEmp 로 변환하여 PayrollCalcService 에 전달
     */
    @Override
    public PayrollCalcService.SimpleEmp findOneEmployee(String empId) {

        // repo.findActiveEmpForPayrollByEmpId() 는 List<EmpForPayrollProjection> 반환
        return repo.findActiveEmpForPayrollByEmpId(empId)
                .stream()
                .findFirst()
                .map(e -> new PayrollCalcService.SimpleEmp(
                        e.getEmpId(),
                        e.getDeptId(),
                        e.getHireDate()
                ))
                .orElse(null);   // 사원 없으면 null 리턴
    }


    /**
     * ✔ 사원 이름 조회
     *   - 명세서 상세 화면에서 사용
     */
    @Override
    public String getEmpName(String empId) {
        return repo.findEmpNameById(empId)
                .orElse(null);
    }

    /**
     * ✔ 부서명 조회
     */
    @Override
    public String getDeptName(String empId) {
        return repo.findDeptNameById(empId)
                .orElse(null);
    }

    /**
     * ✔ 직급 조회
     *   - 직급 수당 계산(GRADE RULE) 에서 사용
     */
    @Override
    public String getEmpPosition(String empId) {
        return repo.findEmpPositionById(empId)
                .orElse(null);
    }

    /**
     * ✔ 사용 연차 조회 (잔여가 아니라 사용분)
     */
    @Override
    public int getUsedAnnual(String empId) {
        return repo.findUsedAnnualById(empId)
                .orElse(0);
    }

    /**
     * ✔ 특정 연도의 잔여 연차 조회
     *   - 연차수당 계산은 1월에 작년 연차 잔여일수로 계산됨
     */
    @Override
    public int getAnnualRemainForYear(String empId, int year) {
        return repo.findRemainDaysByYear(empId, year)
                .orElse(0);
    }

}
