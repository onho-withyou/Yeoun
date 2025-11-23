package com.yeoun.common.util;

import org.springframework.security.core.Authentication;

public class CommonUtil {
	
	public static Boolean hasRole(Authentication authentication, String roleName) {
		
		if(authentication == null) {
			return false; // 인증정보 없으면 false
		}
		
		// 입력받은 권한이 있으면 true 없으면 false
		return authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(roleName));
	}
}
