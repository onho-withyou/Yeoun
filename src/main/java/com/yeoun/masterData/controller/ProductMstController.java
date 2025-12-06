package com.yeoun.masterData.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.service.ProductMstService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/masterData")
@RequiredArgsConstructor
@Log4j2
public class ProductMstController {

	private final ProductMstService productMstService;
    //기준정보관리(완제품/원재료) 연결페이지
  	@GetMapping("/product")
  	public String product(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		//model.addAttribute("empList", approvalDocService.getEmp());//기안자 목록 불러오기
		return "masterData/product";
 	}
  	
  	@ResponseBody
  	@GetMapping("/product/list")
  	public List<ProductMst> productList(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
  		log.info("productMstService.getProductAll()------------->{}",productMstService.findAll());
		return productMstService.findAll();
  	}
  	//BOM 연결페이지
  	@GetMapping("/bom_stock")
  	public String bomStock(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		//model.addAttribute("empList", approvalDocService.getEmp());//기안자 목록 불러오기
		return "masterData/bom_stock";
 	}
    
}
