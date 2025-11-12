package com.yeoun.common.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.modelmapper.ModelMapper;

import com.yeoun.common.entity.CommonCode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonCodeDTO {
	
	private String codeId; // 코드 고유ID
	private CommonCode parent; // 상위 코드 (자기 참조)
    private Set<CommonCode> children = new LinkedHashSet<>(); // 하위 코드들
	private String codeName; // 코드명
	private Integer codeSeq; // 코드 순번
	private String codeDesc; // 코드 설명
	private Integer levelNo; // 계층 깊이
	private String useYn; // 사용 여부
	private String sysType; // 시스템 구분
	private String createdUser; // 등록자
	private LocalDateTime createdDate; // 등록일
	private String updatedUser; // 수정자
	private LocalDateTime updatedDate; // 수정일
	
	// -----------------------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public CommonCode toEntity() {
		return modelMapper.map(this, CommonCode.class);
	}
	
	// DTO 타입으로 변환
	public static CommonCodeDTO fromEntity(CommonCode commonCode) {
		return modelMapper.map(commonCode, CommonCodeDTO.class);
	}
}
