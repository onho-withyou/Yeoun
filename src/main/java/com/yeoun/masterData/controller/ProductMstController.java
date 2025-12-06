package com.yeoun.masterData.controller;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.service.ProductMstService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import oracle.jdbc.proxy.annotation.Post;

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

	@ResponseBody
	@PostMapping("/product/save")
	public ResponseEntity<java.util.Map<String,Object>> productSave(Model model,
			@AuthenticationPrincipal LoginDTO loginDTO,
			@RequestBody Map<String, Object> param) {
		log.info("param------------->{}", param);
		java.util.Map<String,Object> resp = new java.util.HashMap<>();
		try {
			String empId = (loginDTO != null && loginDTO.getEmpId() != null) ? loginDTO.getEmpId() : "SYSTEM";
			String result = productMstService.saveProductMst(empId, param);
			// service returns "success" or "error: ..."; normalize to structured response
			if (result != null && result.trim().toLowerCase().startsWith("success")) {
				resp.put("status", "success");
				resp.put("message", result);
				return ResponseEntity.ok(resp);
			} else {
				resp.put("status", "error");
				resp.put("message", result == null ? "unknown error" : result);
				return ResponseEntity.status(500).body(resp);
			}
		} catch (Exception e) {
			log.error("productSave error", e);
			resp.put("status", "error");
			resp.put("message", e.getMessage());
			return ResponseEntity.status(500).body(resp);
		}
	}

	@ResponseBody
	@PostMapping("/product/delete")
	public ResponseEntity<java.util.Map<String, Object>> productDelete(Model model,
		@AuthenticationPrincipal LoginDTO loginDTO,
		@RequestBody List<String> rowKeys) {

		log.info("rowKeys------------->{}", rowKeys);
		Map<String, Object> param = new java.util.HashMap<>();
		param.put("rowKeys", rowKeys);
		java.util.Map<String, Object> res = productMstService.deleteProduct(param);
		return ResponseEntity.ok(res);
	}

  	//BOM 연결페이지
  	@GetMapping("/bom_stock")
  	public String bomStock(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
		//model.addAttribute("empList", approvalDocService.getEmp());//기안자 목록 불러오기
		return "masterData/bom_stock";
 	}
    
}
