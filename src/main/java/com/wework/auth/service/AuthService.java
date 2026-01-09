package com.wework.auth.service;

import com.wework.auth.dto.request.LoginRequestDto;
import com.wework.auth.dto.response.LoginResponseDto;
import com.wework.auth.infra.redis.RedisTokenStore;
import com.wework.global.security.JwtTokenProvider;
import com.wework.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;

    /**
     * 로그인 처리 결과를 묶어서 반환하는 내부 DTO
     * - body: 클라이언트에 내려줄 응답(JSON)
     * - refreshToken: HttpOnly Cookie로 세팅할 Refresh Token
     * - refreshTtlSeconds: 쿠키 Max-Age 및 Redis TTL에 사용할 만료(초)
     */
    public record LoginResult(LoginResponseDto body, String refreshToken, long refreshTtlSeconds) {
    }

    /**
     * [AUTH_010] 로그인
     *
     * 처리 흐름
     * 1) Spring Security 인증(AuthenticationManager.authenticate)
     * 2) 인증 성공 시 principal(UserPrincipal) 획득
     * 3) Access/Refresh 토큰 발급
     * 4) Refresh 토큰은 Redis에 저장하여 "재발급 가능 여부"를 서버가 통제
     * 5) 권한(ROLE_*) 목록 추출 후 응답 DTO 구성
     *
     * @param requestDto loginId/password 입력 DTO
     * @return 로그인 응답 본문 + refreshToken(cookie용) + refresh ttl
     */
    public LoginResult login(LoginRequestDto requestDto) {

        // [1] 인증 수행 (ID/PW 검증)
        // - 실패 시 BadCredentialsException 등 예외 발생 → ControllerAdvice에서 401로 매핑 가능
        // - 성공 시 Authentication 객체에 principal/authorities가 채워짐
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getLoginId(),
                        requestDto.getPassword()
                )
        );
        // [2] 인증 주체(principal) 추출
        // - CustomUserDetailsService가 만들어준 UserPrincipal(UserDetails 구현체)
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        // [3] Access / Refresh 토큰 발급
        // - Access: API 호출용(Bearer) / 짧은 만료
        // - Refresh: 재발급용(HttpOnly Cookie) / 긴 만료 + 서버(Redis) 저장
        JwtTokenProvider.TokenWithMeta access =
                jwtTokenProvider.createAccessToken(userPrincipal.getEmpId(), userPrincipal.getLoginId());
        JwtTokenProvider.TokenWithMeta refresh =
                jwtTokenProvider.createRefreshToken(userPrincipal.getEmpId(), userPrincipal.getLoginId());
        // [4] Refresh 토큰의 jti를 Redis에 저장 (TTL 포함)
        // - 재발급 시 "Redis에 존재하는 refresh jti"만 허용 → 로그아웃/강제로그아웃 시 즉시 무효화 가능
        redisTokenStore.storeRefresh(refresh.jti(), userPrincipal.getEmpId(), refresh.ttlSeconds());
        // [5] 권한(ROLE_*) 목록 추출
        // - 프론트 메뉴/버튼 제어 및 사용자 요약정보 제공 목적
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        // [6] 프론트 응답(JSON) 구성
        // - AccessToken은 body로 반환
        // - RefreshToken은 body에 포함하지 않고(보안) Controller에서 HttpOnly Cookie로 내려주는 방식 권장
        LoginResponseDto responseDto = LoginResponseDto.builder()
                .accessToken(access.token())
                .expiresIn(access.ttlSeconds())
                .user(LoginResponseDto.UserSummary.builder()
                        .empId(userPrincipal.getEmpId())
                        .loginId(userPrincipal.getLoginId())
                        .name("임시") // TODO: employee 테이블에서 name 조회로 교체
                        .roles(roles)
                        .build()
                ).build();
        // [7] Controller가 쿠키 세팅을 할 수 있도록 refreshToken/TTL을 함께 반환
        return new LoginResult(responseDto, refresh.token(), refresh.ttlSeconds());
    }// class end
} // class
