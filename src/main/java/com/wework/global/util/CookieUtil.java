package com.wework.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

/**
 * Cookie 생성 및 삭제를 위한 유틸리티 클래스.
 *
 * <p>
 * HttpOnly, Secure, SameSite 등 브라우저의 보안 설정을 명확하게 제어하기 위해<br>
 * Spring의 {@link ResponseCookie} 를 활용하여 표준적인 Cookie 문자열을 생성한다.
 * </p>
 *
 * <p><b>환경별 설정 가이드:</b></p>
 * <ul>
 *     <li><b>개발 환경(로컬, HTTP):</b> secure=false</li>
 *     <li><b>운영 환경(HTTPS):</b> secure=true</li>
 *     <li><b>SameSite=Lax (기본 추천)</b> – 브라우저가 CSRF 위험을 적절히 완화</li>
 *     <li><b>SameSite=None</b> – 크로스 사이트 쿠키 전송 필요 시 사용 (단, 반드시 secure=true 필요)</li>
 * </ul>
 *
 * <p>
 * 쿠키는 Response Header의 <code>Set-Cookie</code>로 내려보낸다.
 * </p>
 */
public class CookieUtil {

    /** 유틸 클래스이므로 인스턴스 생성 방지 */
    private CookieUtil(){}

    /**
     * HttpOnly 쿠키 생성 및 응답 헤더에 추가.
     *
     * @param response       HttpServletResponse 객체
     * @param name           쿠키 이름
     * @param value          쿠키 값
     * @param maxAgeSeconds  만료 시간(초 단위). 0 또는 음수면 즉시 만료 처리.
     * @param secure         Secure 옵션 여부 (운영 HTTPS= true / 개발 HTTP= false)
     * @param sameSite       SameSite 옵션 (Lax / Strict / None)
     * @param path           쿠키 적용 경로 (null이면 "/")
     */
    public static void addHttpOnlyCookie(
            HttpServletResponse response,
            String name,
            String value,
            long maxAgeSeconds,
            boolean secure,
            String sameSite,
            String path
    ){
        // ResponseCookie는 SameSite 옵션을 명확하게 지원
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path(path == null ? "/" : path)
                .maxAge(maxAgeSeconds)
                .sameSite(sameSite == null ? "Lax" : sameSite) // Lax / Strict / None
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 쿠키 삭제 메서드.
     * 같은 이름의 쿠키를 값="" + maxAge=0 으로 재설정하여 즉시 만료시킨다.
     *
     * @param response HttpServletResponse
     * @param name     삭제할 쿠키 이름
     * @param secure   Secure 옵션(운영 환경에서는 true)
     * @param sameSite SameSite 옵션
     * @param path     적용 경로
     */
    public static void deleteCookie(
            HttpServletResponse response,
            String name,
            boolean secure,
            String sameSite,
            String path
    ){
        addHttpOnlyCookie(response, name, "", 0, secure, sameSite, path);
    }
}
