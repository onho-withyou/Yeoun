//package com.yeoun.production.controller;
//
//import com.yeoun.auth.dto.LoginDTO;
//import com.yeoun.production.dto.PlanCreateRequestDTO;
//import com.yeoun.production.dto.PlanDetailDTO;
//import com.yeoun.production.dto.ProductionPlanListDTO;
//import com.yeoun.production.service.ProductionPlanService;
//import com.yeoun.sales.dto.OrderPlanSuggestDTO;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/production")
//public class ProductionPlanController {
//
//    private final ProductionPlanService planService;
//
//
//    /* ============================
//       1) 생산계획 목록 페이지
//       ============================ */
//    @GetMapping("/plan")
//    public String planPage() {
//        return "production/plan_list";
//    }
//
//
//    /* ============================
//       2) 생산계획 목록 데이터(JSON)
//       ============================ */
//    @GetMapping("/list")
//    @ResponseBody
//    public List<ProductionPlanListDTO> getPlanList() {
//        return planService.getPlanList();
//    }
//
//
//    /* ============================
//       3) 생산계획 작성 페이지
//       ============================ */
//    @GetMapping("/create")
//    public String planCreatePage() {
//        return "production/plan_create";
//    }
//
//
//    /* ============================
//       4) 수동 생산계획 생성
//       ============================ */
//    @PostMapping("/create/submit")
//    @ResponseBody
//    public Map<String, Object> createPlan(
//            @RequestBody PlanCreateRequestDTO request,
//            @AuthenticationPrincipal LoginDTO login
//    ) {
//
//        String planId = planService.createPlan(
//                request.getItems(),
//                login.getEmpId(),
//                request.getMemo()
//        );
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("planId", planId);
//
//        return response;
//    }
//
//
//    /* ============================
//       5) 생산 추천 목록 조회(JSON)
//       ============================ */
//    @GetMapping("/suggest")
//    @ResponseBody
//    public List<OrderPlanSuggestDTO> getPlanSuggestions(
//            @RequestParam(value = "group", required = false) String group
//    ) {
//        return planService.getPlanSuggestions(group);
//    }
//
//
//    /* ============================
//       6) 자동 생산계획 생성
//       ============================ */
//    @PostMapping("/plan/auto-create")
//    @ResponseBody
//    public Map<String, Object> autoCreatePlan(
//            @RequestBody List<Map<String, Object>> req,
//            @AuthenticationPrincipal LoginDTO login
//    ) {
//
//        Map<String, Object> result = new HashMap<>();
//
//        try {
//            String planIds = planService.createAutoPlan(req, login.getEmpId());
//            result.put("success", true);
//            result.put("planIds", planIds);
//        } catch (Exception e) {
//            result.put("success", false);
//            result.put("message", e.getMessage());
//        }
//
//        return result;
//    }
//    
//    /* ============================
//    7) 생산계획 상세 모달
//    ============================ */
//    @GetMapping("/plan/detail/{planId}")
//    @ResponseBody
//    public PlanDetailDTO getPlanDetailModal(@PathVariable String planId) {
//        return planService.getPlanDetailForModal(planId);
//    }
//
//}
