package com.yeoun.auth.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity	// 스프링 시큐리티 기능 설정 클래스로 지정
public class WebSecurityConfig {
	
	// 시큐리티 정보 조회에 사용할 서비스 클래스 주입
	private final CustomUserDetailsService userDetailsService;
	
	public WebSecurityConfig(CustomUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	// 스프링 시큐리티 보안 필터 설정
	// => 리턴타입이 SecurityFilterChain 타입을 리턴하는 메서드여야 함
	// => 메서드 파라미터에 HttpSecurity 타입을 선언하여 보안 처리용 객체를 자동 주입
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		// HttpSecurity 객체의 다양한 메서드를 메서드 체이닝 형태로 호출하여 스프링 시큐리티 관련 설정을 수행하고
		// 마지막에 build() 메서드 호출하여 HttpSecurity 객체 생성하여 리턴
		return httpSecurity
				// --------- 요청에 대한 접근 허용 여부 등의 요청 경로에 대한 권한 설정 -------
				.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
						
						// 1) 스프링이 인식하는 "공통 정적 리소스" 허용
						.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						
						// 2) 프로젝트에서 실제 쓰는 정적 경로들 "명시" 허용
						.requestMatchers("/assets/**", "/css/**", "/custom_bg/**", "/icon/**", "/js/**").permitAll()
						
						// 3) 로그인 페이지 자체는 누구나 접근 가능
						.requestMatchers("/", "/main", "/login").permitAll()
						
						
						.anyRequest().authenticated() // 그 외 모든 요청은 인증된 사용자만 접근 가능
//						.anyRequest().denyAll() // 그 외의 모든 요청은 거부
					)
					// 
					.formLogin(login -> login
						.loginPage("/login") // 로그인 폼 요청에 사용할 URL 지정(컨트롤러에서 매핑 처리)
						.loginProcessingUrl("/login") // 로그인 폼에서 제출된 데이터 처리용 요청 주소(자동으로 POST 방식으로 처리됨)
						// => 여기서 지정한 경로는 컨트롤러에서 별도의 매핑 불필요(단, 로그인 처리 과정에서 부가적인 기능 추가할 경우 매핑 필요)
						// => 이 과정에서 UserDetailsService(또는 구현체) 객체의 loadByUsername() 메서드가 자동으로 호출됨
						.usernameParameter("empId") // 로그인 과정에서 사용할 사용자명(username)을 사원번호(empId)로 지정(기본값 : username)
						.passwordParameter("empPwd") // 로그인 과정에서 사용할 패스워드 지정(기본값 : password)
//						.defaultSuccessUrl("/main", true) // 로그인 성공 시 항상 리디렉션 할 기본 URL 설정
						.successHandler(new CustomAuthenticationSuccessHandler())  // 로그인 성공 시 별도의 추가 작업을 처리할 핸들러 지정
						.permitAll() // 로그인 경로 관련 요청 주소를 모두 허용
					) 
					// ---------- 로그아웃 처리 설정 ---------
					.logout(logout -> logout
						.logoutUrl("/logout") // 로그아웃 요청 URL 지정(주의! POST 방식 요청으로 취급함)
						.logoutSuccessUrl("/login") // 로그아웃 성공 후 리디렉션 할 URL 지정
						.permitAll()
					)
					//
					.build();
    }

	// 패스워드 인코더로 사용할 빈 등록
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
