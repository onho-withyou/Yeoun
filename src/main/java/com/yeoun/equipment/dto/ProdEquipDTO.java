package com.yeoun.equipment.dto;

import java.time.LocalDateTime;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProdEquipDTO {
	private Long equipId;
	private String equipTypeId;
	private String equipName;
	private String lineId;
	private String status;
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;
	private String remark;
}
