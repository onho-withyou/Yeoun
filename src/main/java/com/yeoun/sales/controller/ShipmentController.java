package com.yeoun.sales.controller;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.sales.dto.ShipmentListDTO;
import com.yeoun.sales.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales/shipment")   // ✅ 출하 관련 URL은 전부 /sales/shipments 아래로
public class ShipmentController {

    private final ShipmentService shipmentService;

    /** 1) 출하관리 화면 (GET)  */
    @GetMapping
    public String shipmentPage() {
        // 메뉴에서 th:href="@{/sales/shipments}" 로 연결
        return "sales/shipment_list";
    }

    /** 2) 출하 목록 조회 (GET, AJAX) */
    @GetMapping("/list")
    @ResponseBody
    public List<ShipmentListDTO> list(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return shipmentService.search(startDate, endDate, keyword, status);
    }

    /** 3) 출하 예약 (POST) */
    @PostMapping("/reserve")
    @ResponseBody
    public String reserveShipment(
            @RequestParam String orderId,
            @AuthenticationPrincipal LoginDTO login
    ) {
        String empId = login.getEmpId();
        return shipmentService.reserveShipment(orderId, empId);
    }
}
