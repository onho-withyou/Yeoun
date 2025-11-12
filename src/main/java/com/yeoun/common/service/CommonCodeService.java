package com.yeoun.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.common.entity.CommonCode;
import com.yeoun.common.repository.CommonCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonCodeService {
	
	private final CommonCodeRepository commonCodeRepository;
	
	// 은행 코드 목록 조회
	public List<CommonCode> getBankList() {
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq("BANK", "Y");
	}

}
