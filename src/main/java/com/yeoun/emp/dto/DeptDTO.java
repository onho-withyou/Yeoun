package com.yeoun.emp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;
import org.springframework.data.annotation.CreatedDate;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeptDTO {

	// 부서ID
	private String deptId;
	
	// 부서명
	private String deptName;
	
	// 상위 부서ID (자기참조)
	private String parentDeptId;
	
	// 부서장ID
	private String managerEmpId;
	
	// 사용여부
	private String useYn = "Y";
	
	// 등록일시
	private LocalDateTime createdDate;	
	
	
	
	// -----------------------------------------------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	// ModelMapper 객체의 map() 메서드를 활용하여 객체 변환 수행
	// 1) DeptDTO -> Dept(엔티티) 타입으로 변환하는 toEntity() 메서드 정의
	public Dept toEntity() {
		return modelMapper.map(this, Dept.class);
	}
	
	// 2) Dept -> DeptDTO 로 변환하는 fromEntity() 메서드 정의
	public static DeptDTO fromEntity(Dept dept) {
		return modelMapper.map(dept, DeptDTO.class);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
