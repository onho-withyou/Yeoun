package com.yeoun.setting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.approval.controller.ApprovalController;

import lombok.extern.log4j.Log4j2;
@Controller
@RequestMapping("/setting")
@Log4j2
public class SettingController {

	//setting 연결페이지
    @GetMapping("/setting_doc")
    public String settingDoc() {
        return "setting/setting_doc";
    }
}