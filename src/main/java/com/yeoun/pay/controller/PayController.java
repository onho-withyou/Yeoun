package com.yeoun.pay.controller;

import com.yeoun.pay.dto.PayRunDTO;          // 폼 검증용 DTO (예: runId 제외, calcType/status 등)
import com.yeoun.pay.entity.PayRun;          // 엔티티
import com.yeoun.pay.service.PayRunService;  // 서비스
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pay")
@Log4j2
@RequiredArgsConstructor
public class PayController {

    // 생성자 주입(RequiredArgsConstructor 이용)
    private final PayRunService payRunService;

    // ============================= [뷰: 페이지 이동] =============================

    // 급여 기준정보 페이지
    @GetMapping("/pay_rule")
    public String payRule() {
        return "redirect:/pay/rule";  // ★ 모델 채우는 컨트롤러로 위임
    }    
  
    // 급여 계산(실행/이력) 메인 페이지 - 목록 기본 로딩
    @GetMapping("/payroll_payslip")
    public String payrollPayslip(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "runId,desc") String sort,
                                 Model model) {
        String[] sp = sort.split(",");
        Sort s = (sp.length == 2)
                ? Sort.by(Sort.Direction.fromString(sp[1]), sp[0])
                : Sort.by(Sort.Direction.DESC, sp[0]);

        Page<PayRun> runs = payRunService.findAll(PageRequest.of(page, size, s));
        model.addAttribute("page", runs);
        model.addAttribute("sort", sort);
        return "pay/payroll_payslip"; // templates/pay/payroll_payslip.html
    }

    // 급여 명세서 페이지
    @GetMapping("/emp_pay")
    public String empPay() {
        return "pay/emp_pay"; // templates/pay/emp_pay.html
    }
    
    @GetMapping("/pay_run")
    public String payRunPage() {
        return "pay/pay_run"; // templates/pay/pay_run.html
    }


    // 급여 실행 등록 폼(입력 폼에 쓸 빈 DTO를 모델에 담아 전달)
    @GetMapping("/run/regist")
    public String runRegistForm(Model model) {
        model.addAttribute("payRunDTO", new PayRunDTO());
        return "pay/pay_run_regist_form"; // templates/pay/pay_run_regist_form.html
    }

    // 급여 실행 등록 처리(검증 포함)
    @PostMapping("/run/regist")
    public String runRegist(@Valid @ModelAttribute("payRunDTO") PayRunDTO dto,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            // 검증 실패 시 폼으로 다시 이동
            return "pay/pay_run_regist_form";
        }
        // 서비스에서 DTO -> 엔티티 변환 및 저장
        PayRun saved = payRunService.save(dto);
        log.info("Created PayRun: {}", saved.getRunId());

        // 등록 후 목록 화면으로 이동(필요하면 메시지 flash attribute 사용)
        return "redirect:/pay/payroll_payslip";
    }

    // ============================= [API: JSON 데이터] =============================

    // 급여 실행 목록(페이지네이션)
    // EX) /pay/api/runs?page=0&size=10&sort=runId,desc
    @GetMapping("/api/runs")
    @ResponseBody
    public Page<PayRun> list(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "runId,desc") String sort) {
        String[] sp = sort.split(",");
        Sort s = (sp.length == 2)
                ? Sort.by(Sort.Direction.fromString(sp[1]), sp[0])
                : Sort.by(Sort.Direction.DESC, sp[0]);
        return payRunService.findAll(PageRequest.of(page, size, s));
    }

    // 급여 실행 단건 조회
    @GetMapping("/api/runs/{id}")
    @ResponseBody
    public ResponseEntity<PayRun> get(@PathVariable Long id) {
        return payRunService.findByIdOptional(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 급여 실행 등록(API)
    @PostMapping("/api/runs")
    @ResponseBody
    public ResponseEntity<PayRun> create(@RequestBody @Valid PayRunDTO dto) {
        return ResponseEntity.ok(payRunService.save(dto));
    }

    // 급여 실행 수정(API)
    @PutMapping("/api/runs/{id}")
    @ResponseBody
    public ResponseEntity<PayRun> update(@PathVariable Long id, @RequestBody @Valid PayRunDTO dto) {
        return ResponseEntity.ok(payRunService.update(id, dto));
    }

    // 급여 실행 삭제(API)
    @DeleteMapping("/api/runs/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        payRunService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
