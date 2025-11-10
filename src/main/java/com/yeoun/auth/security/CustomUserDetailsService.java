package com.yeoun.auth.security;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {
	
	private final EmpRepository empRepository;

	@Override
	public UserDetails loadUserByUsername(String empId) throws UsernameNotFoundException {
		log.info("◆◆◆◆◆◆◆◆◆◆◆◆◆ 사용자 정보 조회용 username : " + empId);
		
		Emp emp = empRepository.findByEmpIdWithRoles(empId)
				.orElseThrow(() -> new UsernameNotFoundException(empId + " : 사원 조회 실패!"));
		log.info("◆◆◆◆◆◆◆◆◆◆◆◆◆ 사용자 정보 : " + emp);
		
		ModelMapper modelMapper = new ModelMapper();
		LoginDTO loginDTO = modelMapper.map(emp, LoginDTO.class);
		
		return loginDTO;
	}
	
	
	
	
}
