package com.yeoun.inbound.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class InboundDTO {
	private String inboundId;
	private LocalDateTime expectArrivalDate;
	private String inboundStatus;
	private String materialId;
	private String prodId;
}
