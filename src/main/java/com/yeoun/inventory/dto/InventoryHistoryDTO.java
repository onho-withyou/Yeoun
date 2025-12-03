package com.yeoun.inventory.dto;

import java.time.LocalDateTime;

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
    private String itemId;           // 품목 ID

    private String prevLocationId;   // 이전 위치
    private String currentLocationId;// 현재 위치

    private String empId;            // 작업자
    private String workType;         // 작업 종류 (입고, 이동, 출고, 폐기, 증가, 감소)

    private Long prevAmount;         // 이전 수량
    private Long currentAmount;      // 현재 수량

    private String reason;           // 사유

    private LocalDateTime createdDate; // 등록 일시

    // Entity -> DTO 변환
    public static InventoryHistoryDTO fromEntity(InventoryHistory history) {
        InventoryHistoryDTO dto = new InventoryHistoryDTO();
        dto.setIvHistoryId(history.getIvHistoryId());
        dto.setLotNo(history.getLotNo());
        dto.setItemId(history.getItemId());
        dto.setPrevLocationId(history.getPrevLocationId());
        dto.setCurrentLocationId(history.getCurrentLocationId());
        dto.setEmpId(history.getEmpId());
        dto.setWorkType(history.getWorkType());
        dto.setPrevAmount(history.getPrevAmount());
        dto.setCurrentAmount(history.getCurrentAmount());
        dto.setReason(history.getReason());
        dto.setCreatedDate(history.getCreatedDate());
        return dto;
    }
}
