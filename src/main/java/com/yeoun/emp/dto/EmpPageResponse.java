package com.yeoun.emp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 사원 목록 화면 검색 + 페이징을 위함
@Getter
@AllArgsConstructor
public class EmpPageResponse {
	
	private List<EmpListDTO> content;
	private int page;
	private int size;
	private long totalElement;
	private int totalPages;

}
