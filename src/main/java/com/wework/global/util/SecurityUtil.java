package com.wework.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    // 생성자 방지
    private SecurityUtil(){}

    /**
     * 현재 인증된 사용자의 empId(PK)를 반환합니다.
     *
     * <p>
     * Spring Security Context에 저장된 Authentication 객체에서
     * principal 값을 추출하여 사용자 고유 식별자(empId)를 long 타입으로 반환합니다.
     * </p>
     *
     * <p>
     * 지원하는 principal 타입은 다음과 같습니다:
     * <ul>
     *   <li><code>Long</code> — 토큰 파싱 과정에서 직접 empId가 Long 형태로 저장된 경우</li>
     *   <li><code>String</code> — empId가 문자열 형태로 저장된 경우</li>
     * </ul>
     * </p>
     *
     * <p>
     * principal이 존재하지 않거나 지원하지 않는 타입일 경우 예외가 발생합니다.
     * </p>
     *
     * @return 현재 로그인한 사용자의 empId
     * @throws IllegalStateException 인증 정보 또는 principal이 없거나 타입이 올바르지 않은 경우
     */
    public static long getCurrentEmpId(){
        // [1] SecurityContext 에서 Authentication 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // [2] auth 또는 principal 이 null 이면 exception 발생
        if(auth == null || auth.getPrincipal() == null) throw new IllegalStateException("인증 정보가 없습니다.");
        // [3] principal이 empId(long/String)로 들어오는 경우 => long 으로 반환
        if(auth.getPrincipal() instanceof Long l) return l;
        if(auth.getPrincipal() instanceof String s) return Long.parseLong(s);
        // [4] 커스텀 UserDetails 등 예상하지 않은 타입인 경우 예외 발생
        throw new IllegalStateException("principle 타입 확인 필요");
    } // func end

} // func end
