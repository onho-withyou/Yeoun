package com.yeoun.approval.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yeoun.approval.dto.ApprovalFormDTO;

@Mapper
public interface ApprovalFormMapper {

	// 기본 결재권자 가져오기
	List<ApprovalFormDTO> findDefaultApproverList(@Param("empId") String empId);

}
