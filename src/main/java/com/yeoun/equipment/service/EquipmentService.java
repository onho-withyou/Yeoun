package com.yeoun.equipment.service;

import com.yeoun.equipment.dto.EquipmentTypeCreateRequest;
import com.yeoun.equipment.dto.ProdEquipDTO;
import com.yeoun.equipment.entity.ProdEquip;
import com.yeoun.equipment.repository.ProdEquipRepository;
import com.yeoun.masterData.dto.EquipmentDTO;
import com.yeoun.masterData.dto.ProdLineDTO;
import com.yeoun.masterData.entity.Equipment;
import com.yeoun.masterData.repository.EquipmentRepository;
import com.yeoun.masterData.repository.ProdLineRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ProdLineRepository prodLineRepository;
    private final ProdEquipRepository prodEquipRepository;

    // ===================================================
    // 설비 마스터 목록
    public List<EquipmentDTO> loadAllEquipmentTypes() {
        return equipmentRepository.findAll().stream()
                .map(EquipmentDTO::fromEntity)
                .toList();
    }

    // ===================================================
    // 라인 목록
    public List<ProdLineDTO> loadAllLines() {
        return prodLineRepository.findAll().stream()
                .map(ProdLineDTO::fromEntity)
                .toList();
    }
    
    // ===================================================
    // 보유 설비 목록
	public List<ProdEquipDTO> loadAllEquipments() {
		List<ProdEquip> equips = prodEquipRepository.findAll();
		List<ProdEquipDTO> list = new ArrayList<>();
		for (ProdEquip equip : equips) {
			ProdEquipDTO dto = ProdEquipDTO.builder()
							.equipId(equip.getEquipId())
							.equipTypeId(equip.getEquipment().getEquipId())
							.equipName(equip.getEquipName())
							.lineId(equip.getLine().getLineId())
							.status(equip.getStatus())
							.createdDate(equip.getCreatedDate())
							.updatedDate(equip.getUpdatedDate())
							.remark(equip.getRemark())
							.build();
			
			list.add(dto);
		}
		return list;
	}

	public void createEquipmentType(EquipmentTypeCreateRequest req) {
		
		EquipmentDTO dto = EquipmentDTO.builder()
				.equipId(req.getEquipId())
				.koName(req.getKoName())
				.equipName(req.getEquipName())
				.remark(req.getRemark())
				.build();
		
		Equipment equipment = dto.toEntity();
		equipmentRepository.save(equipment);
		
	}

}
