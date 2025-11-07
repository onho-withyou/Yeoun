package com.yeoun.approval.cotroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/approval")
@Log4j2
public class ApprovalCotroller {

	//전자결재 연결페이지
    @GetMapping("/approval_doc")
    public String approvalDoc() {
        return "approval/approval_doc";
    }
	
}
