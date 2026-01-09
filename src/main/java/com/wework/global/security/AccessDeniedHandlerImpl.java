package com.wework.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 403 Forbidden(인가 실패) 응답을 처리하는 AccessDeniedHandler 구현체.
 *
 * <p>
 * 인증(Authentication)은 되었지만, 요청한 자원에 접근할 수 있는
 * 권한(Authorization)이 부족한 경우 호출된다.
 * </p>
 *
 * <p>
 * 발생하는 대표 상황:
 * <ul>
 *     <li>로그인은 성공했으나 필요한 ROLE을 가지고 있지 않을 때</li>
 *     <li>SecurityConfig에서 특정 API 접근을 제한한 경우</li>
 *     <li>@PreAuthorize("hasRole('ADMIN')") 등의 권한 검증 실패</li>
 * </ul>
 * </p>
 *
 * <p>
 * SecurityConfig 의 exceptionHandling(accessDeniedHandler) 설정을 통해 적용되며,<br>
 * API 서버에서는 표준 JSON 형태의 에러 구조로 응답하는 것이 일반적이다.
 * </p>
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * 인가(Authorization) 실패 시 실행되며,
     * 403 상태코드 + JSON 메시지를 반환한다.
     *
     * @param request  요청 객체
     * @param response 응답 객체
     * @param accessDeniedException 권한 부족 예외 정보
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        // 403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // JSON 응답
        response.setContentType("application/json;charset=UTF-8");

        // 단순 메시지 출력 (실제 환경에서는 공통 에러 Response DTO 사용 가능)
        response.getWriter().write(
                "{\"code\":\"FORBIDDEN\",\"message\":\"권한이 없습니다.\"}"
        );
    }
}
