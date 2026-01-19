package com.wework.global.security;

import com.wework.auth.infra.redis.RedisTokenStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
 * <p>2026.01.09 블랙리스트 관련 코드 추가</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 생성/검증을 담당하는 Provider */
    private final JwtTokenProvider jwtTokenProvider;

    /** loginId 기반으로 사용자 정보를 로드하는 UserDetailsService 구현체 */
    private final CustomUserDetailsService userDetailsService;

    private final RedisTokenStore redisTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("[JWT] uri={}", request.getRequestURI());
        log.debug("[JWT] Authorization={}", request.getHeader("Authorization"));

        // [1] Authorization 헤더 추출
        String auth = request.getHeader("Authorization");

        // [2] "Bearer " 형식의 토큰인지 확인
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);   // "Bearer " 이후의 실제 토큰 값

            // [3] 토큰 유효성 검증
            if (jwtTokenProvider.validate(token)) {

                // [4] access 토큰 여부 확인
                String type = jwtTokenProvider.getType(token);
                if (!"access".equals(type)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // [5] 블랙리스트 여부 체크
                String jti = jwtTokenProvider.getJti(token);
                if (redisTokenStore.isBlacklisted(jti)) {
                    log.info("[JWT] blacklisted access token. jti={}", jti);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter()
                            .write("{\"message\":\"로그아웃된 토큰입니다.\"}");
                    return; // 🔴 반드시 return
                }

                // [6] 토큰에서 loginId 추출
                String loginId = jwtTokenProvider.getLoginId(token);

                // [7] loginId 로 사용자 정보 조회 (권한, 계정 상태 등 포함)
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

                // [8] 인증 객체 생성 (비밀번호는 null, 권한은 userDetails에서 가져옴)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // [9] 요청 정보(IP, 세션 등)을 Authentication details에 셋팅
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // [10] SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } // if end
        } // if end

        // [11] 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    } // func end
} // class end