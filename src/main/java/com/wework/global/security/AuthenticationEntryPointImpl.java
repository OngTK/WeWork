package com.wework.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401 Unauthorized(인증 실패) 응답을 처리하는 EntryPoint 구현체.
 *
 * <p>
 * Spring Security에서 인증되지 않은 사용자가
 * 보호된 자원(API 엔드포인트)에 접근할 경우 호출된다.
 * </p>
 *
 * <p>
 * 일반적으로 발생하는 상황:
 * <ul>
 *   <li>JWT가 없거나(Bearer 토큰 없음)</li>
 *   <li>JWT가 유효하지 않거나(서명 오류, 만료)</li>
 *   <li>SecurityContext에 Authentication이 설정되지 않은 경우</li>
 * </ul>
 * </p>
 *
 * <p>
 * SecurityConfig 의 exceptionHandling(authenticationEntryPoint) 설정을 통해 적용되며,
 * API 서버에서는 JSON 형태로 에러를 반환하는 것이 일반적이다.
 * </p>
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    /**
     * 인증 실패 시 실행되며, 401 상태코드 + JSON 메시지를 반환한다.
     *
     * @param request 요청 객체
     * @param response 응답 객체
     * @param authException 인증 실패 예외
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // JSON 응답 헤더 설정
        response.setContentType("application/json;charset=UTF-8");

        // 단순 메시지 출력 (실제 환경에서는 표준 에러 Response 형식을 사용할 수 있음)
        response.getWriter().write(
                "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"
        );
    } // func end
} // class end