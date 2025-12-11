package com.yeoun.equipment.controller;

import com.yeoun.equipment.service.EquipmentService;
import com.yeoun.masterData.dto.EquipmentDTO;
import com.yeoun.masterData.dto.ProdLineDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/equipment")
@RequiredArgsConstructor
@Log4j2
public class EquipmentController {

    private final EquipmentService equipmentService;

    // ===================================================
    // 설비, 라인 기준정보 페이지
    @GetMapping("/master")
    public String master(Model model) {
        return "masterData/equipment_line";
    }

    // ===================================================
    // 설비 기준정보 불러오기
    @GetMapping("/equip/data")
    @ResponseBody
    public List<EquipmentDTO> equipData() {
        List<EquipmentDTO> list = equipmentService.loadAllEquipments();
        log.info("list :::::::: " + list);
        return list;
    }

    // ===================================================
    // 라인 기준정보 불러오기
    @GetMapping("/line/data")
    @ResponseBody
    public List<ProdLineDTO> lineData() {
        List<ProdLineDTO> list = equipmentService.loadAllLines();
        log.info("list :::::::: " + list);
        return list;
    }
    
    @GetMapping("/list")
    public String list(Model model) {
    	return "equipment/list";
    }
    
    @GetMapping("/line")
    public String line(Model model) {
    	return "equipment/line";
    }


}
