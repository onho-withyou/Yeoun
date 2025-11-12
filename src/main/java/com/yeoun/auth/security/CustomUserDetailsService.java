package com.yeoun.auth.security;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// 스프링 시큐리티에서 인증 처리(로그인 등)를 수행하는 서비스 클래스 정의
@Service
@RequiredArgsConstructor
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {
	
	private final EmpRepository empRepository;
	private final DeptRepository deptRepository;

	// =========================================================================================
	// 스프링 시큐리티에서 사용자 정보 조회에 사용될 loadUserByUsername() 메서드 오버라이딩
	@Override
	public UserDetails loadUserByUsername(String empId) throws UsernameNotFoundException { // 사원번호를 사용자명으로 사용
		log.info("◆◆◆◆◆◆◆◆◆◆◆◆◆ 사용자 정보 조회용 username : " + empId);
		
		// empId 를 사용하여 Emp 엔티티 조회
		Emp emp = empRepository.findByEmpIdWithDeptAndRoles(empId)
                .orElseThrow(() -> new UsernameNotFoundException(empId + " : 사원 조회 실패!"));
		log.info("◆◆◆◆◆◆◆◆◆◆◆◆◆ 사용자 정보 : " + emp);
		
		// ModelMapper 설정(STRICT) + 모호 필드 명시 매핑
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
        .setMatchingStrategy(MatchingStrategies.STRICT) // 모호성 줄이기
        .setSkipNullEnabled(true);
		
		modelMapper.typeMap(Emp.class, LoginDTO.class)
		        .addMappings(m -> {
		            m.skip(LoginDTO::setDeptId);
		            m.skip(LoginDTO::setDeptName);
		            m.skip(LoginDTO::setEmpRoles);   // 권한도 직접 넣을 거라 skip
		            m.map(Emp::getEmpPwd, LoginDTO::setEmpPwd); // 패스워드 명시 매핑
		        });
		
		LoginDTO loginDTO = modelMapper.map(emp, LoginDTO.class);
		
		// 부서정보: Emp에서 바로
		Dept dept = emp.getDept();
		if (dept != null) {
		    loginDTO.setDeptId(dept.getDeptId());
		    loginDTO.setDeptName(dept.getDeptName());
		}

		// 권한 리스트: LoginDTO.getAuthorities()에서 사용하므로 그대로 세팅
		loginDTO.setEmpRoles(emp.getEmpRoles());
		
		log.info("◆◆◆◆◆◆◆◆◆◆◆◆◆ 로그인 결과 LoginDTO : {}", loginDTO);
		
		// 사용자 인증 정보가 저장된 객체(UserDetails 타입) 리턴
		// UserDetails 의 구현체인 LoginDTO 객체 리턴 시 UserDetails 타입으로 업캐스팅
		return loginDTO; 
	}
	
	
}
