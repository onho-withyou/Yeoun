package com.yeoun.inventory.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.inventory.entity.InventoryHistory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InventoryHistoryDTO {
    private Long ivHistoryId;        // 이력 ID
    private String lotNo;            // LOT 번호
    private String itemName;           // 재고이름

    private String prevLocationId;   // 이전 위치
    private String prevLocationName;
    private String currentLocationId;// 현재 위치
    private String currentLocationName;

    private String empId;            // 작업자
    private String workType;         // 작업 종류 (입고, 이동, 출고, 폐기, 증가, 감소)
    
    private Long moveAmount;         // 이동 수량
    private Long prevAmount;         // 이전 수량
    private Long currentAmount;      // 현재 수량

    private String reason;           // 사유

    private LocalDateTime createdDate; // 등록 일시
    
    // ---------------------------------------------------------------------------
    private static ModelMapper modelMapper = new ModelMapper();
    // Entity -> DTO 변환
    public static InventoryHistoryDTO fromEntity(InventoryHistory history) {
        InventoryHistoryDTO dto = modelMapper.map(history, InventoryHistoryDTO.class);
        if(history.getPrevWarehouseLocation() != null) {
        	dto.setPrevLocationId(history.getPrevWarehouseLocation().getLocationId());
        	dto.setPrevLocationName(history.getPrevWarehouseLocation().getLocationName());
        }
        if(history.getCurrentWarehouseLocation() != null) {
        	dto.setCurrentLocationId(history.getCurrentWarehouseLocation().getLocationId());
        	dto.setCurrentLocationName(history.getCurrentWarehouseLocation().getLocationName());
        }
        
        return dto;
    }
}
