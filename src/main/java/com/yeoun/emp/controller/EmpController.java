package com.yeoun.emp.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.service.CommonCodeService;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpDetailDTO;
import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.emp.dto.EmpPageResponse;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.emp.service.EmpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/emp")
@Log4j2
@RequiredArgsConstructor
public class EmpController {
	
	private final EmpService empService;
	private final CommonCodeService commonCodeService;
	private final DeptRepository deptRepository;
	private final PositionRepository positionRepository;
	
	// 사원 등록/수정 폼 공통 셀렉트 박스 세팅
	private void setupEmpFormCommon(Model model) {
		model.addAttribute("deptList", deptRepository.findActive());
		model.addAttribute("positionList", positionRepository.findActive());
		model.addAttribute("bankList", commonCodeService.getBankList());
	}

	// =============================================================================================
	// 뷰페이지로 포워딩 시 입력값 검증으로 활용되는 DTO 객체(빈 객체)를 Model 객체에 담아 함께 전달
	// GET : 사원 등록 폼
	@GetMapping("/regist")
	public String registEmp(Model model) {
		
		if (!model.containsAttribute("empDTO")) {
	        model.addAttribute("empDTO", new EmpDTO());
	    }
	    if (!model.containsAttribute("mode")) {
	        model.addAttribute("mode", "create");
	    }

		
		setupEmpFormCommon(model);
		
		return "emp/emp_form";
	}

	// POST 방식으로 요청되는 "/regist" 요청 매핑
	// => 파라미터로 전달되는 값들을 EmpDTO 객체에 바인딩
	// => EmpDTO 객체에 바인딩되는 파라미터들에 대한 Validation Check(입력값 검증) 수행을 위해 
	//    메서드 선언부에 EmpDTO 타입 파라미터를 선언하고 @Valid 어노테이션을 적용
	// => 체크 결과를 뷰페이지에서 활용하기 위해 뷰페이지에서 접근할 DTO 객체 이름을 @ModelAttribute 어노테이션 속성으로 명시
	// => 전달된 파라미터들이 EmpDTO 객체에 바인딩되는 시점에 입력값 검증을 수행하고 이 결과를 BindingResult 타입 파라미터에 저장해줌
	@PostMapping("/regist")
	public String regist(@ModelAttribute("empDTO") @Validated(EmpDTO.Regist.class) EmpDTO empDTO,
						 BindingResult bindingResult,
						 RedirectAttributes rttr) {
		log.info(">>>>>>>>>>>>>> empDTO : " + empDTO);
		log.info(">>>>>>>>>>>>>> bindingResult.getAllErrors : " + bindingResult.getAllErrors());
		
		// 1) 입력값 검증 결과가 true 일 때(검증 오류 발생 시) 다시 입력폼으로 포워딩
		if (bindingResult.hasErrors()) {
	        rttr.addFlashAttribute("empDTO", empDTO);
	        rttr.addFlashAttribute("mode", "create");
	        // BindingResult도 같이 플래시에 태워줘야 th:errors 가 동작함
	        rttr.addFlashAttribute(
	            "org.springframework.validation.BindingResult.empDTO", bindingResult);

	        return "redirect:/emp/regist";
	    }
		
		try {
	        // 2) 서비스 호출 (이메일/연락처 중복 검사)
	        empService.registEmp(empDTO);
	    } catch (IllegalStateException e) {
	        // 3) 중복 등 비즈니스 에러 처리
	        String msg = e.getMessage();

	        // 메시지 내용 보고 필드에 바인딩 (서비스에서 메시지 던진다는 가정)
	        if (msg.contains("주민등록번호")) {
	        	bindingResult.rejectValue("rrn", "duplicate", msg);
	        } else if (msg.contains("이메일")) {
	            bindingResult.rejectValue("email", "duplicate", msg);
	        } else if (msg.contains("연락처")) {
	            bindingResult.rejectValue("mobile", "duplicate", msg);
	        } else {
	            // 혹시 모를 기타 에러는 글로벌 에러로
	            bindingResult.reject("empRegistError", msg);
	        }
		
	        // 다시 폼으로
	        rttr.addFlashAttribute("empDTO", empDTO);
	        rttr.addFlashAttribute("mode", "create");
	        rttr.addFlashAttribute(
	            "org.springframework.validation.BindingResult.empDTO", bindingResult);

	        return "redirect:/emp/regist";
	    }

	    // 4) 정상 등록 시
	    rttr.addFlashAttribute("msg", "사원 등록이 완료되었습니다.");
	    return "redirect:/emp";
	}
	
	// ====================================================================================
	// 사원 메인 페이지 (현황 + 등록 버튼 있는 화면)
	@GetMapping("")
	public String empMainPage(@AuthenticationPrincipal LoginDTO user,
							  Model model,
							  @RequestParam(value = "keyword", required = false) String keyword,
							  @RequestParam(value = "deptId", required = false) String deptId) {
		
		boolean isHr  = user.hasRole("HR_ADMIN") || user.hasRole("SYS_ADMIN");
	    boolean isMgr = user.hasRole("DEPT_MANAGER");
		
	    if (isMgr && !isHr) {
	        deptId = user.getDeptId(); // 부서장은 자기 부서만
	    }
	    
	    // 부서 셀렉트 옵션용
	    model.addAttribute("deptList", deptRepository.findActive());
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("deptId", deptId);
	    model.addAttribute("isHr", isHr);
	    model.addAttribute("isMgr", isMgr);
	    
		return "/emp/emp_list";
	}
	
	// AJAX 데이터 로딩 + 검색 + 페이징
	@ResponseBody
	@GetMapping("/data")
	public EmpPageResponse getEmpList (@AuthenticationPrincipal LoginDTO user,
									   @RequestParam(defaultValue = "0", name = "page") int page,
									   @RequestParam(defaultValue = "10", name = "size") int size,
									   @RequestParam(defaultValue = "", name = "keyword") String keyword,
									   @RequestParam(required = false, name = "deptId") String deptId) {
		
		boolean isHr  = user.hasRole("HR_ADMIN") || user.hasRole("SYS_ADMIN");
	    boolean isMgr = user.hasRole("DEPT_MANAGER");

	    if (isMgr && !isHr) {
	        deptId = user.getDeptId(); // 부서장은 자기 부서만
	    }
		
		// 서비스에서 Page<EmpListDTO> 받아오기
		Page<EmpListDTO> empPage = empService.getEmpList(page, size, keyword, deptId);
		
		return new EmpPageResponse(empPage.getContent(),
								   empPage.getNumber(),
								   empPage.getSize(),
								   empPage.getTotalElements(),
								   empPage.getTotalPages());
	}
	
	
	// ====================================================================================
	// 사원 정보 상세 조회
	@ResponseBody
	@GetMapping("/detail/{empId}")
	public EmpDetailDTO getEmpDetail(@PathVariable("empId") String empId) {
		return empService.getEmpDetail(empId);
	}
	
	// ====================================================================================
	// 사원 정보 수정
	@GetMapping("/edit/{empId}")
	public String editEmp(@PathVariable("empId") String empId, Model model) {
		
		// 수정용 DTO 조회
	    EmpDTO empDTO = empService.getEmpForEdit(empId);
	    
		// 공통 모델 세팅
	    model.addAttribute("empDTO", empDTO);
		model.addAttribute("mode", "edit");
		model.addAttribute("formAction", "/emp/edit");
		
		setupEmpFormCommon(model);
		
		// 상태 셀렉트용 공통코드 (재직/휴직/퇴직 등) 
		model.addAttribute("statusList", commonCodeService.getCodes("EMP_STATUS"));
		
		return "emp/emp_form";
	}
	
	@PostMapping("/edit")
	public String updateEmp(@ModelAttribute("empDTO") @Validated(EmpDTO.Edit.class) EmpDTO empDTO, 
							BindingResult bindingResult,
							Model model,
							RedirectAttributes rttr) {
		
		// 입력값 검증 실패 시
		if (bindingResult.hasErrors()) {
			empDTO.setRrnMasked(empService.maskRrn(empDTO.getRrn()));
			model.addAttribute("mode", "edit");
			model.addAttribute("formAction", "/emp/edit");
			setupEmpFormCommon(model); 
			model.addAttribute("statusList", commonCodeService.getCodes("EMP_STATUS"));
			
			return "emp/emp_form";
		}
		
		try {
	        // 2) 서비스 호출 (이메일/연락처 중복 검사 등)
	        empService.updateEmp(empDTO);

	    } catch (IllegalStateException e) {
	        String msg = e.getMessage();

	        // 등록이랑 패턴 맞춰서 필드별 에러 매핑
	        if (msg.contains("이메일")) {
	            bindingResult.rejectValue("email", "duplicate", msg);
	        } else if (msg.contains("연락처")) {
	            bindingResult.rejectValue("mobile", "duplicate", msg);
	        } else {
	            bindingResult.reject("empEditError", msg);
	        }

	        // 에러난 상태로 다시 폼
	        model.addAttribute("mode", "edit");
	        model.addAttribute("formAction", "/emp/edit");
	        setupEmpFormCommon(model);
	        model.addAttribute("statusList", commonCodeService.getCodes("EMP_STATUS"));

	        return "emp/emp_form";
	    }

	    // 3) 정상 수정 시
	    rttr.addFlashAttribute("msg", "정보 수정이 완료되었습니다.");
	    return "redirect:/emp";
	}

	
	
}
