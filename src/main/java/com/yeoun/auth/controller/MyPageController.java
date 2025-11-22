package com.yeoun.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.auth.dto.PasswordChangeDTO;
import com.yeoun.common.service.CommonCodeService;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.dto.EmpDetailDTO;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.PositionRepository;
import com.yeoun.emp.service.EmpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyPageController {
	
	private final EmpService empService;
    private final BCryptPasswordEncoder encoder;
    private final DeptRepository deptRepository;
    private final PositionRepository positionRepository;
    private final CommonCodeService commonCodeService;

    // 1.  내 정보 JSON (모달용)
    @GetMapping("/info")
    @ResponseBody
    public EmpDetailDTO getMyInfo(@AuthenticationPrincipal LoginDTO loginUser) {

        return  empService.getEmpDetail(loginUser.getEmpId());
    }
    
	// 내 정보 수정
	@GetMapping("/info/edit")
	public String editMyInfo(@AuthenticationPrincipal LoginDTO loginUser,
							 Model model) {
		
		EmpDTO empDTO = empService.getEmpForEdit(loginUser.getEmpId());
		
		model.addAttribute("empDTO", empDTO);
		model.addAttribute("formAction", "/my/info/update");
		model.addAttribute("mode", "edit");
		
		// emp_form.html에서 필요한 공통 select box 세팅
	    model.addAttribute("deptList", deptRepository.findActive());
	    model.addAttribute("positionList", positionRepository.findActive());
	    model.addAttribute("bankList", commonCodeService.getBankList());
	    model.addAttribute("statusList", commonCodeService.getCodes("EMP_STATUS"));
		
		return "emp/emp_form";
	}
	
	@PostMapping("/info/update")
	public String updateMyInfo(@AuthenticationPrincipal LoginDTO loginUser,
	                           @ModelAttribute("empDTO") @Validated(EmpDTO.Edit.class) EmpDTO empDTO,
	                           BindingResult bindingResult,
	                           Model model,
	                           RedirectAttributes rttr) {

		// 로그인한 본인 아이디 강제 세팅
	    empDTO.setEmpId(loginUser.getEmpId()); 
	    
	    // 1) Bean Validation 실패 폼 다시 보여주기
	    if (bindingResult.hasErrors()) {
	    	empDTO.setRrnMasked(empService.maskRrn(empDTO.getRrn()));
	        model.addAttribute("formAction", "/my/info/update");
	        model.addAttribute("mode", "edit");
	        model.addAttribute("deptList", deptRepository.findActive());
	        model.addAttribute("positionList", positionRepository.findActive());
	        model.addAttribute("bankList", commonCodeService.getBankList());
	        model.addAttribute("statusList", commonCodeService.getCodes("EMP_STATUS"));

	        return "emp/emp_form";
	    }
	    
	    // 2) 중복 검사 등 비즈니스 에러 처리
	    try {
	        empService.updateEmp(empDTO);
	    } catch (IllegalStateException e) {

	        String msg = e.getMessage();

	        if (msg.contains("이메일")) {
	            bindingResult.rejectValue("email", "duplicate", msg);
	        } else if (msg.contains("연락처")) {
	            bindingResult.rejectValue("mobile", "duplicate", msg);
	        } else {
	            bindingResult.reject("empEditError", msg);
	        }

	        model.addAttribute("formAction", "/my/info/update");
	        model.addAttribute("mode", "edit");
	        model.addAttribute("deptList", deptRepository.findActive());
	        model.addAttribute("positionList", positionRepository.findActive());
	        model.addAttribute("bankList", commonCodeService.getBankList());
	        model.addAttribute("statusList", commonCodeService.getCodes("EMP_STATUS"));

	        return "emp/emp_form";
	    }

	    // 3) 성공
	    rttr.addFlashAttribute("msg", "내 정보가 수정되었습니다.");
	    return "redirect:/main";  
	}

    // 2. 비밀번호 변경 폼 
    @GetMapping("/password")
    public String changePasswordForm(Model model) {
        model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        return "auth/password_change";   // templates/auth/password_change.html
    }

    // 3. 비밀번호 변경 처리 
    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeDTO") PasswordChangeDTO dto,
                                 BindingResult bindingResult,
                                 Authentication auth,
                                 RedirectAttributes rttr,
                                 Model model) {
    	
        LoginDTO login = (LoginDTO) auth.getPrincipal();
        String empId = login.getEmpId();
        
        // 1) 폼 검증 에러
        if (bindingResult.hasErrors()) {
            return "auth/password_change";
        }

        // 2) 새 비밀번호/확인 일치 확인
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "새 비밀번호와 확인이 일치하지 않습니다.");
            return "auth/password_change";
        }

        // 3) 현재 비밀번호 확인
        String currentEncodedPwd = login.getEmpPwd();   
        
        if (!encoder.matches(dto.getCurrentPassword(), currentEncodedPwd)) {
            bindingResult.rejectValue("currentPassword", "invalid", "현재 비밀번호가 올바르지 않습니다.");
            return "auth/password_change";
        }

        // 4) 실제 비밀번호 변경
        empService.changePassword(empId, dto.getNewPassword());
        
        // 5) 성공 메시지 + 로그아웃 플래그를 플래시 속성으로 전달
        rttr.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
        rttr.addFlashAttribute("logoutAfterChange", true);

        // 같은 페이지로 리다이렉트
        return "redirect:/my/password";
    }

}
