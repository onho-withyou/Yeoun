package com.yeoun.equipment.service;

import com.yeoun.masterData.dto.EquipmentDTO;
import com.yeoun.masterData.dto.ProdLineDTO;
import com.yeoun.masterData.repository.EquipmentRepository;
import com.yeoun.masterData.repository.ProdLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ProdLineRepository prodLineRepository;

    // ===================================================
    // 설비 목록
    public List<EquipmentDTO> loadAllEquipments() {
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

}
