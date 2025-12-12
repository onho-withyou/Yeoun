package com.yeoun.masterData.dto;

import com.yeoun.masterData.entity.Equipment;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class EquipmentDTO {
    private String equipId;
    private String koName;
    private String equipName;
    private String remark;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private static ModelMapper modelMapper = new ModelMapper();
    public Equipment toEntity(){
        return modelMapper.map(this, Equipment.class);
    }

    public static EquipmentDTO fromEntity(Equipment entity){
        return modelMapper.map(entity, EquipmentDTO.class);
    }

}

