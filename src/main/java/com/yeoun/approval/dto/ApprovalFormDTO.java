package com.yeoun.approval.dto;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.approval.entity.ApprovalForm;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class ApprovalFormDTO {

	@NotBlank(message="양식코드는 자동코드 입니다!")
	private String formCode; //양식 코드
	
    @NotBlank(message="양식이름은 필수 입력값입니다!")
	private String formName; //양식 이름
	
	private String deptId; //부서id
	
	private String approver1; //결재권한자1
	
	private String approver2; //결재권한자2
	
	private String approver3; //결재권한자3
	
	private String approver1Name; // 결재권한자1 이름
	
	private String approver2Name; // 결재권한자2 이름
	
	private String approver3Name; // 결재권한자3 이름
	
    private String empName;
	// -----------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	public static ApprovalFormDTO fromEntity(ApprovalForm approvalForm) {
		return modelMapper.map(approvalForm, ApprovalFormDTO.class);
	}
	
	public ApprovalForm toEntity() {
		return modelMapper.map(this, ApprovalForm.class);
	}
}
