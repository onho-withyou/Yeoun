package com.yeoun.production.controller;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.production.dto.PlanCreateRequestDTO;
import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.entity.ProductionPlanItem;
import com.yeoun.production.service.ProductionPlanService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/production")
public class ProductionPlanController {

    private final ProductionPlanService planService;
    
    /** 생산계획 목록 화면 */
    @GetMapping("/plan")
    public String planPage() {
        return "production/plan_list";   
    }


    /** =============================
     * 생산계획 생성
     * ============================= */
    @PostMapping("/create")
    @ResponseBody
    public String createPlan(
            @RequestBody PlanCreateRequestDTO request,
            @AuthenticationPrincipal LoginDTO login
    ) {
        String empId = login.getEmpId();
        String memo = request.getMemo();

        return planService.createPlan(request.getItems(), empId, memo);
    }


    /** =============================
     * 생산계획 목록 조회
     * ============================= */
    @GetMapping("/list")
    @ResponseBody
    public List<ProductionPlan> getPlanList() {
        return planService.getPlanList();
    }


    /** =============================
     * 생산계획 상세 조회
     * ============================= */
    @GetMapping("/{planId}")
    @ResponseBody
    public ProductionPlan getPlanDetail(@PathVariable String planId) {
        return planService.getPlanDetail(planId);
    }

    /** =============================
     * 생산계획 상세 item 리스트 조회
     * ============================= */
    @GetMapping("/{planId}/items")
    @ResponseBody
    public List<ProductionPlanItem> getPlanItems(@PathVariable String planId) {
        return planService.getPlanItems(planId);
    }
}
