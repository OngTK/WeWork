package com.wework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration  // Spring config 클래스임을 명시
public class SecurityConfig {

    /**
     * SecurityFilterChain
     * <p> - Spring Security의 핵심 필터 체인 설정
     * <p> - 기존 WebSecurityConfigurerAdapter는 폐기
     * <p> - Spring boot 표준 방식
     */
    @Bean    // 빈 등록
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                /*
                 * CSRF(Cross-Site Request Forgery) 보호 비활성화
                 * - 세션 기반 + POST 폼 환경에서 주로 필요
                 * - 현재 API 서버 + 개발 단계이므로 CSRF 비활성화
                 * - 추후 로그인/폼 기반 인증 시 다시 활성화 고려
                */ 
                .csrf(csrf -> csrf.disable())
                /*
                * 요청(URL)별 접근 권한 설정
                */
                .authorizeHttpRequests(auth -> auth
                        /*
                        * 선언한 URL은 인증 없이 접근 허용
                        * */
                        .requestMatchers("/ping", "/actuator/**").permitAll()
                        /*
                        * 위에서 선언한 URL 그 외에 모든 요청도 허용
                        * - 개발 초기 단계에서 사용
                        * */
                        .anyRequest().permitAll()
                )
                /*
                * 기본 로그인 폼 비활성화
                * - /login 페이지 자동 생성 방지
                * - React 연동 or API 서버에서는 사용하지 않음
                */
                .formLogin(form -> form.disable())
                /*
                * HTTP Basic 인증 비활성화
                * - Authorization : Bazic 헤더 사용 안 함
                * - REST API + JWT 방식으로 갈 예정이므로 비활성화
                */
                .httpBasic(basic -> basic.disable());
        /*
        * 설정한 SecurityFilterChain 반환
        * */
        return http.build();
    } // func end
} // class end
