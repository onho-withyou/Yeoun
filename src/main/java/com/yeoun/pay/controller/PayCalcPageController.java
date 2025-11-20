package com.yeoun.pay.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.pay.dto.EmpForPayrollProjection;
import com.yeoun.pay.dto.PayCalcStatusDTO;
import com.yeoun.pay.dto.PayslipDetailDTO;
import com.yeoun.pay.dto.PayslipViewDTO;
import com.yeoun.pay.repository.EmpNativeRepository;
import com.yeoun.pay.repository.PayrollPayslipRepository;
import com.yeoun.pay.service.PayCalcStatusService;
import com.yeoun.pay.service.PayrollCalcQueryService;
import com.yeoun.pay.service.PayrollCalcService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/pay/calc")
@RequiredArgsConstructor
public class PayCalcPageController {

    private final PayCalcStatusService statusSvc;
    private final PayrollCalcService payrollCalcService;
    private final PayrollCalcQueryService querySvc;
    private final PayrollPayslipRepository payslipRepo;
    private final PayrollCalcQueryService payrollCalcQueryService;
    private final EmpNativeRepository empNativeRepository;

    /* ==========================
       공통: 최근 12개월 리스트 생성
    ========================== */
    private List<String> getRecentMonths() {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate d = now.minusMonths(i);
            months.add(d.format(DateTimeFormatter.ofPattern("yyyyMM")));
        }
        return months;
    }

    /* ==========================
        공통: 현재 yyyymm 계산
    ========================== */
    private String getTargetMonth(String yyyymm) {
        return (yyyymm == null || yyyymm.isBlank())
                ? PayrollCalcService.currentYymm()
                : yyyymm;
    }

    /* ==========================
        AJAX - 상태 조회
    ========================== */
    @GetMapping("/status")
    @ResponseBody
    public PayCalcStatusDTO status(@RequestParam("yyyymm") String yyyymm) {
        return statusSvc.getStatus(yyyymm);
    }
    
    /* ==========================
		    기본 진입점
		    /pay/calc → 전체 급여 계산 화면으로 연결
		========================== */
		@GetMapping
		public String redirectToAll(@RequestParam(name = "yyyymm", required = false) String yyyymm,
		                            Model model) {
		    return pageAll(yyyymm, model);
		}

    
	    /* ==========================
	    0) 급여 계산 메인 선택 화면
	    - pay_calc_main.html
	 ========================== */
	 @GetMapping("/main")
	 public String calcMain() {
	     return "pay/pay_calc_main";
	 }


    /* ==========================
        1) 전체 계산 화면 (HTML)
        - pay_calc_all.html
    ========================== */
    @GetMapping("/all")
    public String pageAll(@RequestParam(name = "yyyymm", required = false) String yyyymm,
                          Model model) {

        String mm = getTargetMonth(yyyymm);

        List<String> months = getRecentMonths();
        List<PayslipViewDTO> slips = querySvc.findForView(mm);
        var totals = querySvc.totals(slips);

        model.addAttribute("months", months);
        model.addAttribute("yyyymm", mm);
        model.addAttribute("status", statusSvc.getStatus(mm));
        model.addAttribute("slips", slips);
        model.addAttribute("sumPay", totals[0]);
        model.addAttribute("sumDed", totals[1]);
        model.addAttribute("sumNet", totals[2]);

        return "pay/pay_calc_all";
    }


    /* ==========================
        2) 사원별 계산 화면 (HTML)
        - pay_calc_emp.html
    ========================== */
    @GetMapping("/emp")
    public String pageEmp(@RequestParam(name = "yyyymm", required = false) String yyyymm,
                          Model model) {

        String mm = getTargetMonth(yyyymm);

        model.addAttribute("months", getRecentMonths());
        model.addAttribute("yyyymm", mm);
        model.addAttribute("empList", empNativeRepository.findActiveEmpList()); 
        // ↑ 사원 리스트 조회용 NativeQuery (없는 경우 내가 만들어줄게)

        return "pay/pay_calc_emp";
    }


    /* ==========================
        [POST] 가계산 (전체/사원별 자동 분기)
    ========================== */
    @PostMapping("/simulate")
    public String simulate(
            @RequestParam(name = "yyyymm") String yyyymm,
            @RequestParam(name = "overwrite", defaultValue = "true") boolean overwrite,
            @RequestParam(name = "empId", required = false) String empId,
            RedirectAttributes ra) {

        int cnt;

        if (empId != null && !empId.isBlank()) {
            cnt = payrollCalcService.simulateOne(yyyymm, empId, overwrite);
            ra.addFlashAttribute("msg",
                    "사원 " + empId + " 가계산 완료 (" + cnt + "건)");

            return "redirect:/pay/calc/emp?yyyymm=" + yyyymm;
        } else {
            cnt = payrollCalcService.simulateMonthly(yyyymm, overwrite);
            ra.addFlashAttribute("msg",
                    "전체 가계산 완료 (" + cnt + "건)");

            return "redirect:/pay/calc/all?yyyymm=" + yyyymm;
        }
    }


    /* ==========================
        [POST] 확정 (전체/사원별 자동 분기)
    ========================== */
    @PostMapping("/confirm")
    public String confirm(
            @RequestParam(name = "yyyymm") String yyyymm,
            @RequestParam(name = "overwrite", defaultValue = "true") boolean overwrite,
            @RequestParam(name = "empId", required = false) String empId,
            RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginEmpId = auth.getName();
        String loginEmpName = empNativeRepository.findEmpNameByEmpId(loginEmpId);

        int cnt;

        if (empId != null && !empId.isBlank()) {
            cnt = payrollCalcService.confirmOne(yyyymm, empId, overwrite, loginEmpName);
            ra.addFlashAttribute("msg",
                    "사원 " + empId + " 확정 완료 (" + cnt + "건)");

            return "redirect:/pay/calc/emp?yyyymm=" + yyyymm;
        } else {
            cnt = payrollCalcService.confirmMonthly(yyyymm, overwrite, loginEmpName);
            ra.addFlashAttribute("msg",
                    loginEmpName + " 님이 전체 급여 확정 완료했습니다.");

            return "redirect:/pay/calc/all?yyyymm=" + yyyymm;
        }
    }

    /* ==========================
        상세 조회 (AJAX)
    ========================== */
    @GetMapping("/detail")
    @ResponseBody
    public PayslipDetailDTO detail(
            @RequestParam("yyyymm") String yyyymm,
            @RequestParam("empId") String empId) {

        return payrollCalcQueryService.getPayslipDetail(yyyymm, empId);
    }
    
    
    /* ==========================
    		개별 사원 정보 조회 
		========================== */
        
    @GetMapping("/emp/info")
    @ResponseBody
    public Map<String, Object> getEmpInfo(
            @RequestParam("empId") String empId,
            @RequestParam("yyyymm") String yyyymm
    ) {

        EmpForPayrollProjection emp = empNativeRepository
                .findActiveEmpForPayrollByEmpId(empId)
                .stream()
                .findFirst()
                .orElse(null);

        if (emp == null) return Map.of("error", "NOT_FOUND");

        // DB에서 상태 가져오기
        String calcStatus = payslipRepo
                .findCalcStatus(yyyymm, empId)
                .orElse("READY");

        return Map.of(
                "empId", emp.getEmpId(),
                "empName", emp.getEmpName(),
                "deptName", emp.getDeptName(),
                "posName", emp.getPosName(),
                "calcStatus", calcStatus
        );
    }

	    
	    /* ==========================
	    사원별 가계산 (AJAX)
	========================== */
    @PostMapping("/emp/simulate")
    @ResponseBody
    public PayslipDetailDTO simulateOneAjax(
            @RequestParam(name = "empId") String empId,
            @RequestParam(name = "yyyymm") String yyyymm
    ) {
        payrollCalcService.simulateOne(yyyymm, empId, true);
        return payrollCalcQueryService.getPayslipDetail(yyyymm, empId);
    }
	
	
	/* ==========================
	    사원별 확정 (AJAX)
	========================== */
    @PostMapping("/emp/confirm")
    @ResponseBody
    public PayslipDetailDTO confirmOneAjax(
            @RequestParam(name = "empId") String empId,
            @RequestParam(name = "yyyymm") String yyyymm
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginEmpId = auth.getName();
        String loginEmpName = empNativeRepository.findEmpNameByEmpId(loginEmpId);

        payrollCalcService.confirmOne(yyyymm, empId, true, loginEmpName);
        return payrollCalcQueryService.getPayslipDetail(yyyymm, empId);
    }

	/* ==========================
    AJAX - 사원 검색 (자동완성)
	========================== */
	@GetMapping("/searchEmployee")
	@ResponseBody
	public List<Map<String, String>> searchEmployee(@RequestParam("keyword") String keyword) {

	    List<EmpForPayrollProjection> list = empNativeRepository.findActiveEmpForPayroll();
	    List<Map<String, String>> result = new ArrayList<>();

	    for (EmpForPayrollProjection e : list) {
	        if (e.getEmpName().contains(keyword)) {
	            Map<String, String> item = new HashMap<>();
	            item.put("empId", e.getEmpId());
	            item.put("empName", e.getEmpName());
	            result.add(item);
	        }
	    }
	    return result;
	}



}
