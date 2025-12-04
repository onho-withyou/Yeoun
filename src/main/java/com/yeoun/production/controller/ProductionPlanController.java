package com.yeoun.production.controller;


import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.production.dto.PlanCreateRequestDTO;
import com.yeoun.production.dto.ProductionPlanListDTO;
import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.entity.ProductionPlanItem;
import com.yeoun.production.service.ProductionPlanService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
     * 생산계획 목록 조회(JSON) - DTO 기반
     * ============================= */
    @GetMapping("/list")
    @ResponseBody
    public List<ProductionPlanListDTO> getPlanList() {
        System.out.println("📌 [CONTROLLER] /production/plan/list 호출됨");

        List<ProductionPlanListDTO> list = planService.getPlanList();  // DTO 사용하는 메서드
        System.out.println("📌 [CONTROLLER] 조회건수 = " + list.size());

        return list;
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
    
    /*생산계획 작성 페이지 열기*/
    @GetMapping("/create")
    public String planCreatePage() {
        return "production/plan_create";
    }


//    /** =============================
//     * 생산계획 상세 조회
//     * ============================= */
//    @GetMapping("/{planId}")
//    @ResponseBody
//    public ProductionPlan getPlanDetail(@PathVariable String planId) {
//        return planService.getPlanDetail(planId);
//    }
//
//    /** =============================
//     * 생산계획 상세 item 리스트 조회
//     * ============================= */
//    @GetMapping("/{planId}/items")
//    @ResponseBody
//    public List<ProductionPlanItem> getPlanItems(@PathVariable String planId) {
//        return planService.getPlanItems(planId);
//    }
}
