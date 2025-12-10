package com.yeoun.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialAvailabilityDTO {
	private String prdId;
	private String prdName;
	private String matId;
	private String matName;
	private Double requiredQty;
	private Double stockQty;

}
