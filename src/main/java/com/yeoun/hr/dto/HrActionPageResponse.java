package com.yeoun.hr.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HrActionPageResponse {
	
	private List<HrActionDTO> content; // 현재 페이지 데이터
    private int page;                  // 현재 페이지(0부터)
    private int size;                  // 페이지 크기
    private long totalElements;        // 전체 건수
    private int totalPages;            // 전체 페이지 수

}
