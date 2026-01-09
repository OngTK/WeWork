package com.wework.global.security;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security의 전체 보안 설정 클래스.
 *
 * <p>핵심 역할:
 * <ul>
 *   <li>JWT 기반 인증/인가 체인 구성</li>
 *   <li>CSRF, 세션, Basic/Form 로그인 비활성화</li>
 *   <li>Exception Handler(401, 403) 적용</li>
 *   <li>URL 접근 정책 허용/제한 설정</li>
 *   <li>JwtAuthenticationFilter를 필터 체인에 삽입</li>
 * </ul>
 *
 * <p>WebSecurityConfigurerAdapter는 더 이상 사용되지 않으며,
 * SecurityFilterChain + DSL 방식이 Spring Security의 공식 패턴이다.
 */

@Configuration  // Spring config 클래스임을 명시
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)     // JwtProperties 활성화
public class SecurityConfig {

    // DI ======================================
    // JWT 토큰 검증 및 인증 객체 생성 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // 401 Unauthorized 응답 처리
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    // 403 Forbidden 응답 처리
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    // func ====================================

    /**
     * SecurityFilterChain
     * <p>Spring Security의 요청-응답 필터 체인을 정의하는 핵심 Bean.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                /* -------------------------------------------------------
                 * 1) 기본 보안 옵션 비활성화
                 * ------------------------------------------------------- */
                .csrf(csrf -> csrf.disable())              // JWT 기반이므로 CSRF 불필요
                .cors(Customizer.withDefaults())                                    // CORS 기본 설정
                .sessionManagement(sm ->      // 세션 미사용(Stateless)
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* -------------------------------------------------------
                 * 2) 인증/인가 실패 처리 핸들러
                 * ------------------------------------------------------- */
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((AuthenticationEntryPoint) authenticationEntryPoint)  // 401
                        .accessDeniedHandler((AccessDeniedHandler) accessDeniedHandler)                 // 403
                )

                /* -------------------------------------------------------
                 * 3) 경로별 접근 정책
                 * ------------------------------------------------------- */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()                 // 로그인/회원가입 API 허용
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()      // Preflight 허용
                        .anyRequest().authenticated()                                         // 나머지는 인증 필요
                )

                /* -------------------------------------------------------
                 * 4) 기본 인증 방식 비활성화
                 * ------------------------------------------------------- */
                .formLogin(form -> form.disable())      // /login 페이지 비활성화
                .httpBasic(basic -> basic.disable());    // Basic 인증 비활성화

        /* -------------------------------------------------------
         * 5) JWT 필터 등록
         * - UsernamePasswordAuthenticationFilter 실행 이전에 동작하도록 설정
         * ------------------------------------------------------- */
        http.addFilterBefore((Filter) jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager Bean 등록.
     * <p>Spring Security 5.7+ 에서 AuthenticationConfiguration을 통해 획득.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
} // class end
