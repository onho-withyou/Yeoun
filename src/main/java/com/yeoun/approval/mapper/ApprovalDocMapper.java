package com.yeoun.approval.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.approval.dto.ApprovalDocDTO;
import com.yeoun.approval.dto.ApprovalFormDTO;

@Mapper
public interface ApprovalDocMapper {
	//그리드 결재사항 검색 - 진행해야할 결재만
	List<ApprovalDocDTO> searchApprvalDocGrid1(Map<String, Object> params);
			/*@Param("createDate") String createDate
												,@Param("finishDate") String finishDate
												,@Param("empName") String empName
												,@Param("approvalTitle") String approvalTitle);
*/
}
