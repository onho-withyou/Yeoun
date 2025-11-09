package com.yeoun.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity	// 스프링 시큐리티 기능 설정 클래스로 지정
public class WebSecurityConfig {
	
	// 스프링 시큐리티 보안 필터 설정
	// => 리턴타입이 SecurityFilterChain 타입을 리턴하는 메서드여야 함
	// => 메서드 파라미터에 HttpSecurity 타입을 선언하여 보안 처리용 객체를 자동 주입
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (폼 테스트 시 편함)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // ✅ 모든 요청 인증 없이 허용
            );
        return http.build();
    }

	// 패스워드 인코더로 사용할 빈 등록
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
