package com.yeoun.common.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.common.dto.CommonCodeDTO;
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

	// 연차 산정 기준 조회
	public List<CommonCodeDTO> getAnnualBasis(String parentCodeId) {
		List<CommonCode> codeList = commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq(parentCodeId, "Y");
		
		if (codeList.isEmpty()) {
			throw new NoSuchElementException("해당하는 코드가 없습니다.");
		}
		
		return codeList.stream()
				.map(CommonCodeDTO::fromEntity)
				.collect(Collectors.toList());
	}

}
