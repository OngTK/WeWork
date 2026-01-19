package com.wework.auth.service;

import com.wework.auth.dto.request.LoginRequestDto;
import com.wework.auth.dto.response.LoginResponseDto;
import com.wework.auth.dto.response.TokenReissueResponseDto;
import com.wework.auth.infra.redis.RedisTokenStore;
import com.wework.global.security.JwtTokenProvider;
import com.wework.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
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
     * [AUTH_012] 로그인
     *
     * <P>처리 흐름</P>
     * <P>1) Spring Security 인증(AuthenticationManager.authenticate)</P>
     * <P>2) 인증 성공 시 principal(UserPrincipal) 획득</P>
     * <P>3) Access/Refresh 토큰 발급</P>
     * <P>4) Refresh 토큰은 Redis에 저장하여 "재발급 가능 여부"를 서버가 통제</P>
     * <P>5) 권한(ROLE_*) 목록 추출 후 응답 DTO 구성</P>
     *
     * @param requestDto loginId/password 입력 DTO
     * @return 로그인 응답 본문 + refreshToken(cookie용) + refresh ttl
     */
    public LoginResult login(LoginRequestDto requestDto) {

        Authentication authentication;

        try{
            // [1] 인증 수행 (ID/PW 검증)
            // - 실패 시 BadCredentialsException 등 예외 발생 → ControllerAdvice에서 401로 매핑 가능
            // - 성공 시 Authentication 객체에 principal/authorities가 채워짐
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getLoginId(),
                            requestDto.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // 실패 시 로그인 실패 횟수 누적
            long failCount = redisTokenStore.increaseLoginFail(requestDto.getLoginId(), 30 * 60);
            // 기존 예외를 그대로 던지면 ControllerAdvice에서 401로 매핑 가능
            throw e;
        }


        // [2] 인증 주체(principal) 추출
        // - CustomUserDetailsService가 만들어준 UserPrincipal(UserDetails 구현체)
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // [추가] 로그인 성공 시 로그인 시도 횟수 초기화
        redisTokenStore.clearLoginFail(userPrincipal.getLoginId());

        // [3] Access / Refresh 토큰 발급
        // - Access: API 호출용(Bearer) / 짧은 만료
        // - Refresh: 재발급용(HttpOnly Cookie) / 긴 만료 + 서버(Redis) 저장
        JwtTokenProvider.TokenWithMeta access =
                jwtTokenProvider.createAccessToken(userPrincipal.getEmpId(), userPrincipal.getLoginId());
        JwtTokenProvider.TokenWithMeta refresh =
                jwtTokenProvider.createRefreshToken(userPrincipal.getEmpId(), userPrincipal.getLoginId());
        // [3-1] 강제 로그인을 위해 accessJti 추적용 정보를 redis에 저장
        redisTokenStore.storeAccessJti(userPrincipal.getEmpId(), access.jti(), access.ttlSeconds());

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
                        .name(userPrincipal.getUsername())
                        .roles(roles)
                        .build()
                ).build();
        // [7] Controller가 쿠키 세팅을 할 수 있도록 refreshToken/TTL을 함께 반환
        return new LoginResult(responseDto, refresh.token(), refresh.ttlSeconds());
    } // func end

    /**
     * [AUTH_011] 로그아웃
     * - RefreshToken 쿠키 기반 로그아웃
     * - Redis에서 refresh jti 삭제하여 재발급 차단
     * */
    public void logout(String refreshTokenCookie, String accessTokenHeader){
        // [1] 쿠키가 없거나 비정상이면, 이미 로그아웃된 것으로 보고 종료
        if(refreshTokenCookie == null || refreshTokenCookie.isBlank()){return;}

        // [2] refresh 토큰 파싱/검증
        // 만료/위조 Token이면 Redis 처리 없이 종료
        if(!jwtTokenProvider.validate(refreshTokenCookie)){return;}

        // [3] refresh 타입인지 확인
        String typ = jwtTokenProvider.getType(refreshTokenCookie);
        // refresh가 아니면 종료
        if(!"refresh".equals(typ)){return;}

        // [4] refresh jti 추출 후 redis에서 삭제
        String refreshJti = jwtTokenProvider.getJti(refreshTokenCookie);
        redisTokenStore.deleteRefresh(refreshJti);

        // 2026.01.19 accessToken 삭제 및 블랙리스트 처리코드 추가 ---------------
        // [5] accessToken이 없으면 스킵
        if(accessTokenHeader == null || accessTokenHeader.isBlank()) return;
        // [6] 만료/위조 토큰이면 스킵(이미 무효에 가까움)
        if(!jwtTokenProvider.validate(accessTokenHeader)) return;
        // [7] access typ인지 확인 후, jti를 TTL(남은 시간)과 함께 Redis blacklist에 저장
        String typ2 = jwtTokenProvider.getType(accessTokenHeader);
        if(!"access".equals(typ2)) return;
        String accessJti = jwtTokenProvider.getJti(accessTokenHeader);
        long ttlSeconds = getRemainingTtlSeconds(accessTokenHeader);
        if(ttlSeconds <= 0) return;
        redisTokenStore.blacklistAccess(accessJti,ttlSeconds);
    } // func end

    /**
     * ttl 추출 메소드
     * */
    private long getRemainingTtlSeconds(String token){
        Date exp = jwtTokenProvider.parse(token).getPayload().getExpiration();
        long diffMs = exp.getTime() - System.currentTimeMillis();
        return diffMs <= 0 ? 0 : diffMs/1000 ;
    } // func end


    /**
     * 재생성한 AccessToken 반환용 record
     */
    public record ReissueResult(TokenReissueResponseDto body, String refreshToken, long refreshTtlSeconds){}

    /**
     * [AUTH_12] 토큰 재발급(Refresh Rotation 적용)
     * @param refreshTokenCookie 쿠키에서 읽은 refreshToken(JWT 문자열)
     * @return 새 accessToken 응답 + 새 refreshToken(쿠키 세팅용) + refresh TTL
     * */
    public ReissueResult reissueToken(String refreshTokenCookie){
        // [1] refreshToken 유효성 검증 (서명/만료/구조)
        if(!jwtTokenProvider.validate(refreshTokenCookie)){
            // 유효성 검증 실패 시, Exception 발생
            // IllegalArgumentException
            // - 메서드에 전달된 인자가 유효하지 않음
            throw new IllegalArgumentException("Invalid refresh token");
        }
        // [2] 토큰 타입 검증 (refresh만 허용)
        String typ = jwtTokenProvider.getType(refreshTokenCookie);
        if(!"refresh".equals(typ)){
            throw new IllegalArgumentException("Token type is not refresh");
        }
        // [3] refresh 토큰에서 사용자 식별 및 jti 추출
        long empId = jwtTokenProvider.getEmpId(refreshTokenCookie);
        String loginId = jwtTokenProvider.getLoginId(refreshTokenCookie);
        String refreshJti = jwtTokenProvider.getJti(refreshTokenCookie);
        // [4] Redis에 refresh jti 존재 여부 확인
        // - Redis에 없으면 이미 로그아웃/폐기된 refresh >> 재발급 불가
        if(!redisTokenStore.existsRefresh(refreshJti)){
            throw new IllegalArgumentException("Refresh token not found in store");
        }
        // [5] (Rotation) 기존 refresh 폐기
        redisTokenStore.deleteRefresh(refreshJti);
        // [6] 새 토큰 발급 (access + refresh)
        JwtTokenProvider.TokenWithMeta newAccess = jwtTokenProvider.createAccessToken(empId, loginId);
        JwtTokenProvider.TokenWithMeta newRefresh = jwtTokenProvider.createRefreshToken(empId, loginId);
        // [7] 새 refresh를 Redis 저장
        redisTokenStore.storeRefresh(newRefresh.jti(), empId, newRefresh.ttlSeconds());
        // [8] 응답 Body 구성 (AccessToken만 JSON으로 반환)
        TokenReissueResponseDto body = TokenReissueResponseDto.builder()
                .accessToken(newAccess.token())
                .expiresIn(newAccess.ttlSeconds())
                .build();

        return new ReissueResult(body, newAccess.token(), newAccess.ttlSeconds());
    } // func end


} // class end
