package com.yeoun.emp.dto;

import java.time.LocalDate;

import com.yeoun.emp.entity.Emp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class EmpListDTO {
	
	private LocalDate hireDate;
    private String empId;
    private String empName;
    private String deptName; // 현재 부서명
    private String posName;  // 현재 직급명
    private Integer rankOrder;  // 직급 서열
    private String mobile;
    private String email;
    
    public static EmpListDTO fromEntity(Emp emp) {
        return new EmpListDTO(
            emp.getHireDate(),
            emp.getEmpId(),
            emp.getEmpName(),
            emp.getDept().getDeptName(),
            emp.getPosition().getPosName(),
            emp.getPosition().getRankOrder(),
            emp.getMobile(),
            emp.getEmail()
        );
    }


}
