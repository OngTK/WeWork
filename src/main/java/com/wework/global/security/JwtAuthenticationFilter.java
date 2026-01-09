package com.wework.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * <p>
 * 매 요청마다 한 번씩 실행되며(OncePerRequestFilter),<br>
 * HTTP 헤더의 {@code Authorization: Bearer {token}} 값을 읽어
 * 유효한 JWT인 경우 SecurityContext에 인증 정보를 저장한다.
 * </p>
 *
 * 필터 동작 순서:
 * <ol>
 *   <li>Authorization 헤더 추출</li>
 *   <li>"Bearer " 토큰 형식인지 확인</li>
 *   <li>토큰 유효성 검사(JwtTokenProvider.validate)</li>
 *   <li>토큰에서 loginId 추출 후 UserDetails 조회</li>
 *   <li>UsernamePasswordAuthenticationToken 생성 및 SecurityContext 설정</li>
 *   <li>다음 필터로 체인 전달</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 생성/검증을 담당하는 Provider */
    private final JwtTokenProvider jwtTokenProvider;

    /** loginId 기반으로 사용자 정보를 로드하는 UserDetailsService 구현체 */
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) Authorization 헤더 추출
        String auth = request.getHeader("Authorization");

        // 2) "Bearer " 형식의 토큰인지 확인
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);   // "Bearer " 이후의 실제 토큰 값

            // 3) 토큰 유효성 검증
            if (jwtTokenProvider.validate(token)) {
                // 4) 토큰에서 loginId 추출
                String loginId = jwtTokenProvider.getLoginId(token);

                // 5) loginId 로 사용자 정보 조회 (권한, 계정 상태 등 포함)
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

                // 6) 인증 객체 생성 (비밀번호는 null, 권한은 userDetails에서 가져옴)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // 7) 요청 정보(IP, 세션 등)을 Authentication details에 셋팅
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8) SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } // if end
        } // if end

        // 9) 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    } // func end
} // class end