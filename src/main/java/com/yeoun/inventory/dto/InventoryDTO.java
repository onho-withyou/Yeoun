package com.yeoun.inventory.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InventoryDTO {
	private Long ivId; // 재고Id 
	
//	@NotBlank(message = "로트번호는 필수 입력값입니다.")
	private String lotNo;
	
//	@NotBlank(message = "로케이션ID는 필수 입력값입니다.")
	private String locationId;
	
//	@NotBlank(message = "상품ID는 필수 입력값입니다.")
	private String itemId; // 원자재/부자재, 완제품의 기준정보 고유값
	
//	@NotBlank(message = "재고량은 필수 입력값입니다.")
	private Long ivAmount; // 재고량
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime expirationDate; // 유통기한
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime manufactureDate; // 제조일
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime ibDate; // 입고일

	private Long expectObAmount = 0l; // 출고예정수량
	
//	@NotBlank(message = "재고상태는 필수 입력값입니다.")
	private String ivStatus = "ok"; // 재고상태 : 정상/임박
	
	private String prodName; // 재고 상품이름
	
	private String itemType; // 재고타입(원자재 : RAW, 부자재 : SUB, 완제품: FG)
	
	private String zone; // 존
	
	private String rack; // 랙
	
	private String status; // 상태
	
	
}
