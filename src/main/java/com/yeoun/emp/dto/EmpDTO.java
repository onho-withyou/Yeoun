package com.yeoun.emp.dto;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;

import com.yeoun.emp.entity.Emp;

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
public class EmpDTO {

	@NotBlank(message = "이름은 필수 입력값입니다!")
	@Length(min = 2, max = 20, message = "이름은 2 ~ 20자리 입니다.")
	private String empName;  // 이름			
	
	@NotBlank(message = "성별을 선택해주세요.")
    @Pattern(regexp = "M|F", message = "성별은 M 또는 F만 가능합니다.")
	private String gender;  // 성별 
	
	// 연락처: 공백불가 + 형식(010-0000-0000)
    @NotBlank(message = "연락처는 필수입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처 형식은 010-0000-0000 입니다.")
	private String mobile;  // 연락처		
	
	@NotBlank(message = "이메일은 필수 입력값입니다!")
	@Email(message = "이메일 형식에 맞게 입력해 주세요!")
	private String email;  // 이메일		
	
	@NotBlank(message = "우편번호는 필수 입력값입니다!") 
	private String postCode;  // 우편번호 	
	
	@NotBlank(message = "기본 주소는 필수 입력값입니다!")
	private String address1;  // 기본주소 	
	private String address2;  // 상세주소 	
	
	// 입사일: 필수 + 과거/오늘
    @NotNull(message = "입사일은 필수입니다.")
    @PastOrPresent(message = "입사일은 오늘 또는 과거 날짜만 가능합니다.")
	private LocalDate hireDate;  // 입사일 (엔티티 생성 시 자동 등록)	
	
    // 재직 상태		
	private String status; 
	
	// ERP / MES 구분
	@NotBlank(message = "소속 종류를 선택해주세요.")
	private String empType;
	
	// 부서 
	@NotBlank(message = "부서를 선택해주세요.")
    private String deptId;
	
	// 직급
	@NotBlank(message = "직급을 선택해주세요.")
    private String posCode;
	
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
