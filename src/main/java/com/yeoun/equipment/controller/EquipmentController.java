package com.yeoun.equipment.controller;

import com.yeoun.equipment.dto.EquipmentTypeCreateRequest;
import com.yeoun.equipment.dto.ProdEquipDTO;
import com.yeoun.equipment.service.EquipmentService;
import com.yeoun.masterData.dto.EquipmentDTO;
import com.yeoun.masterData.dto.ProdLineDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public String master(EquipmentTypeCreateRequest req, Model model) {
    	model.addAttribute("equipmentTypeCreateRequest", req);
        return "masterData/equipment_line";
    }

    // ===================================================
    // 설비 기준정보 불러오기
    @GetMapping("/equip/data")
    @ResponseBody
    public List<EquipmentDTO> equipData() {
        List<EquipmentDTO> list = equipmentService.loadAllEquipmentTypes();
        log.info("list :::::::: " + list);
        return list;
    }
    
    // ===================================================
    // 설비 마스터 등록하기
    @PostMapping("/equipType")
    public String createEquipType(
    		@Valid @ModelAttribute EquipmentTypeCreateRequest req,
    		BindingResult bindingResult,
    		Model model) {
    	
    	if (bindingResult.hasErrors()) {
    		model.addAttribute("openEquipmentModal", true);
    		return "masterData/equipment_line";
    	}
    	
    	equipmentService.createEquipmentType(req);
    	return "redirect:/equipment/master";
    }
    
    // ===================================================
    // 설비 마스터 수정하기
    @PatchMapping("/equipType/{id}")
    public void updateEquipType(@PathVariable String id) {
    }
    
    // ===================================================
    // 설비 마스터 삭제하기
    @DeleteMapping("/equipType/{id}")
    public void deleteEquipType(@PathVariable String id) {
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
    
    
    // ===================================================
    // 라인 등록하기
    @PostMapping("/line")
    public void createLine() {
    }
    
    // ===================================================
    // 라인 수정하기
    @PatchMapping("/line/{id}")
    public void updateLine(@PathVariable String id) {
    }
    
    // ===================================================
    // 라인 삭제하기
    @DeleteMapping("/line/{id}")
    public void deleteLine(@PathVariable String id) {
    }
    
    // ===================================================
    // 설비 목록
    @GetMapping("/list")
    public String list(Model model) {
    	List<ProdEquipDTO> list = equipmentService.loadAllEquipments();
    	model.addAttribute("list", list);
    	return "equipment/list";
    }
    
    // ===================================================
    // 보유 설비 등록하기
    @PostMapping("/equipment")
    public void createEquipment() {
    }
    
    // ===================================================
    // 보유 설비 수정하기
    @PatchMapping("/equipment/{id}")
    public void updateEquipment(@PathVariable String id) {
    }
    
    // ===================================================
    // 보유 설비 삭제하기
    @DeleteMapping("/equipment/{id}")
    public void deleteEquipment(@PathVariable String id) {
    }
    
    // ===================================================
    // 라인 목록
    @GetMapping("/line")
    public String line(Model model) {
        List<ProdLineDTO> list = equipmentService.loadAllLines();
        model.addAttribute("list", list);
    	return "equipment/line";
    }


}
