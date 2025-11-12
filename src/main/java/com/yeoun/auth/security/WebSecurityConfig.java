package com.yeoun.auth.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
	// ====================================================================
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
				
				// =========================================================================
		        // 공통: 정적 리소스 및 로그인/회원가입 등 완전 공개 구역
					// 스프링이 인식하는 공통 정적 리소스
					.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
					// 프로젝트에서 실제 쓰는 정적 경로들 명시
					.requestMatchers("/assets/**", "/css/**", "/custom_bg/**", "/icon/**", "/js/**").permitAll()
					
					// 공개 페이지
					.requestMatchers("/", "/main", "/login", "/logout").permitAll()
				
//					// HR/인사 화면: URL은 “로그인만” 통과 → 세부 권한은 메소드 보안에서
//				    .requestMatchers("/emp/**").authenticated()
	                
	            // =========================================================================
                // 그 외 모든 요청은 인증 필요 
					.anyRequest().authenticated() 
				)
				// ---------- 로그인 처리 설정 ---------
				.formLogin(login -> login
					.loginPage("/login") // 로그인 폼 요청에 사용할 URL 지정(컨트롤러에서 매핑 처리)
					.loginProcessingUrl("/login") // 로그인 폼에서 제출된 데이터 처리용 요청 주소(자동으로 POST 방식으로 처리됨)
					// => 여기서 지정한 경로는 컨트롤러에서 별도의 매핑 불필요(단, 로그인 처리 과정에서 부가적인 기능 추가할 경우 매핑 필요)
					// => 이 과정에서 UserDetailsService(또는 구현체) 객체의 loadByUsername() 메서드가 자동으로 호출됨
					.usernameParameter("empId") // 로그인 과정에서 사용할 사용자명(username)을 사원번호(empId)로 지정(기본값 : username)
					.passwordParameter("empPwd") // 로그인 과정에서 사용할 패스워드 지정(기본값 : password)
					.successHandler(new CustomAuthenticationSuccessHandler())  // 로그인 성공 시 별도의 추가 작업을 처리할 핸들러 지정
					.permitAll() // 로그인 경로 관련 요청 주소를 모두 허용
				) 
				// ---------- 로그아웃 처리 설정 ---------
				.logout(logout -> logout
					.logoutUrl("/logout") // 로그아웃 요청 URL 지정(주의! POST 방식 요청으로 취급함)
					.logoutSuccessUrl("/login?logout") // 로그아웃 성공 후 리디렉션 할 URL 지정
					.permitAll()
				)
				// ---------- 자동 로그인 처리 설정 ----------
				.rememberMe(rememberMeCustormizer -> rememberMeCustormizer
						.rememberMeParameter("remember-me") // 자동 로그인 수행을 위한 체크박스 파라미터명 지정(체크 여부 자동으로 판별)
						.key("my-fixed-secret-key") // 서버 재시작해도 이전 로그인에서 사용했던 키 동일하게 사용
						.tokenValiditySeconds(60 * 60 * 24 * 30) // 자동 로그인 토큰 유효기간 설정(30일)
				)
				.build();
    }

	// ====================================================================
	// 패스워드 인코더로 사용할 빈 등록
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
