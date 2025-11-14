package com.yeoun.approval.cotroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.approval.repository.ApprovalDocRepository;
import com.yeoun.approval.service.ApprovalDocService;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.repository.EmpRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
@Log4j2
public class ApprovalCotroller {

	private final ApprovalDocService approvalDocService;
	private final ApprovalDocRepository approvalDocRepository;
	
	//전자결재 연결페이지
    @GetMapping("/approval_doc")
    public String approvalDoc(Model model) {
		log.info("▶ 결재- 기안자명(사번)");		
		List<String> nameList = new ArrayList<String>(Arrays.asList("홍길동", "김철수", "박영희"));
		
		model.addAttribute("getEmpList",nameList);//결재- 기안자 목록 불러오기
		return "approval/approval_doc";
    }
	
}
