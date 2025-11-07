package com.yeoun.emp.dto;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;

import com.yeoun.emp.entity.Emp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmpDTO {

	@NotBlank(message = "이름은 필수 입력값입니다!")
	@Length(min = 2, max = 20, message = "이름은 2 ~ 20자리 필수!")
	private String empName;  // 이름			
	
	private String gender;  // 성별 
	
	private String mobile;  // 연락처		
	
	@NotBlank(message = "이메일은 필수 입력값입니다!")
	@Email(message = "이메일 형식에 맞게 입력해 주세요!")
	private String email;  // 이메일		
	
	@NotBlank(message = "우편번호는 필수 입력값입니다!") 
	private String postCode;  // 우편번호 	
	
	@NotBlank(message = "기본 주소는 필수 입력값입니다!")
	private String address1;  // 기본주소 	
	private String address2;  // 상세주소 	
	
	private LocalDate hireDate;  // 입사일 (엔티티 생성 시 자동 등록)	
	
	private String status;  // 재직 상태		
	
	// 부서 / 직급
//    private String deptId;
//    private String positionId;
	
	private Long photoFileId;  // 사진파일ID (사진 파일 FK)	
	
	private String roleCode;  // 역할코드	
	
	
	
	// -----------------------------------------------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	// ModelMapper 객체의 map() 메서드를 활용하여 객체 변환 수행
	// 1) EmpDTO -> Emp(엔티티) 타입으로 변환하는 toEntity() 메서드 정의
	public Emp toEntity() {
		return modelMapper.map(this, Emp.class);
	}
	
	// 2) Entity -> DTO 로 변환하는 fromEntity() 메서드 정의
	public static EmpDTO fromEntity(Emp emp) {
		return modelMapper.map(emp, EmpDTO.class);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
