package com.yeoun.emp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.service.EmpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/emp")
@RequiredArgsConstructor
@Log4j2
public class EmpController {
	
	private final EmpService empService;

	// =============================================================================================
	// 뷰페이지로 포워딩 시 입력값 검증으로 활용되는 DTO 객체(빈 객체)를 Model 객체에 담아 함께 전달
	// GET : 사원 등록 폼
	@GetMapping("/regist")
	public String registEmp(Model model) {
		model.addAttribute("empDTO", new EmpDTO());
		return "emp/emp_regist";
	}

	// POST 방식으로 요청되는 "/regist" 요청 매핑
	// => 파라미터로 전달되는 값들을 EmpDTO 객체에 바인딩
	// => EmpDTO 객체에 바인딩되는 파라미터들에 대한 Validation Check(입력값 검증) 수행을 위해 
	//    메서드 선언부에 EmpDTO 타입 파라미터를 선언하고 @Valid 어노테이션을 적용
	// => 체크 결과를 뷰페이지에서 활용하기 위해 뷰페이지에서 접근할 DTO 객체 이름을 @ModelAttribute 어노테이션 속성으로 명시
	// => 전달된 파라미터들이 EmpDTO 객체에 바인딩되는 시점에 입력값 검증을 수행하고 이 결과를 BindingResult 타입 파라미터에 저장해줌
	@PostMapping("/regist")
	public String regist(@ModelAttribute("empDTO") @Valid EmpDTO empDTO,
						 BindingResult bindingResult,
						 RedirectAttributes rttr) {
		log.info(">>>>>>>>>>>>>> empDTO : " + empDTO);

		log.info(">>>>>>>>>>>>>> bindingResult.getAllErrors : " + bindingResult.getAllErrors());
		
		// 입력값 검증 결과가 true 일 때(검증 오류 발생 시) 다시 입력폼으로 포워딩
		if(bindingResult.hasErrors()) {
			return "emp/emp_regist";
		}
		
		// EmpService - registEmp() 메서드 호출하여 사원 등록 처리 요청
		empService.registEmp(empDTO);
		
		rttr.addFlashAttribute("msg", "사원 등록이 완료되었습니다.");
		return "redirect:/emp/list";
		
	}
	
	

	// 사원 목록
	@GetMapping("/list")
	public String List() {
		return "emp/emp_list";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
