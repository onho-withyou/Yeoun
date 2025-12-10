package com.yeoun.masterData.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/masterData")
@RequiredArgsConstructor
@Log4j2
public class EquipmentController {

    @GetMapping("/equipment_line")
    public String list(Model model) {
        return "masterData/equipment_line";
    }


}
