package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.service.ProcessMstService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/masterData")
@RequiredArgsConstructor
@Log4j2
public class ProcessMstController {
    private final ProcessMstService processMstService;

    // 공정(라우트/코드)관리 연결페이지
    @GetMapping("/process_mst")
    public String processMst(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
        model.addAttribute("prdMstList", processMstService.getPrdMst()); // 라우트 제품코드 불러오기
        return "masterData/process_mst";
    }

    // 제품별 공정라우트 조회 (AJAX)
    @ResponseBody
    @GetMapping("/process/list")
    public List<RouteHeader> processList(Model model, @AuthenticationPrincipal LoginDTO loginDTO,
            @RequestParam("prdId") String prdId,
            @RequestParam("routeName") String routeName) {
        return processMstService.getRouteHeaderList(prdId, routeName);
    }
    // 공정코드 조회 (AJAX)
    @ResponseBody
    @GetMapping("/processCode/list")
    public List<ProcessMst> processCodeList(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
        return processMstService.getProcessCodeList();
    }  

}
