package com.wework.auth.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Refresh Token 및 Access Token Blacklist를 Redis에 저장·조회·삭제하는 저장소 클래스.
 *
 * <p>
 * Redis를 이용해 토큰 상태를 실시간으로 관리할 수 있으며,
 * JWT 기반 인증 시스템에서 다음과 같은 기능을 제공한다:
 * </p>
 *
 * <ul>
 *     <li><b>Refresh Token 저장</b> – TTL 기반으로 사용자 세션을 유지</li>
 *     <li><b>Refresh Token 존재 여부 확인</b></li>
 *     <li><b>Refresh Token 삭제</b> – 로그아웃/재발급 시</li>
 *     <li><b>Access Token 블랙리스트 등록</b> – 로그아웃 시 즉시 무효화</li>
 *     <li><b>Blacklist 조회</b> – 무효화된 토큰인지 검사</li>
 * </ul>
 *
 * <p>
 * Redis Key Naming Rule:
 * <ul>
 *     <li><code>auth:refresh:{jti}</code> – Refresh Token</li>
 *     <li><code>auth:blacklist:{jti}</code> – Access Token Blacklist</li>
 * </ul>
 * </p>
 *
 * <p>
 * JTI(JWT ID)는 토큰마다 고유한 UUID 같은 값을 사용하여
 * 토큰 단위의 관리가 가능하게 한다.
 * </p>
 */

@Component
@RequiredArgsConstructor
public class RedisTokenStore {

    // RedisTemplate을 통한 Redis 접근
    private final RedisTemplate<String, Object> redisTemplate;

    /* =========================================================
     *  Redis Key 생성 메서드
     * ========================================================= */

    // Refresh Token Key prefix
    private String refreshKey(String jti) {
        return "auth:refresh:" + jti;
    } // func end

    // Access Token Blacklist Key prefix
    private String blacklistKey(String jti) {
        return "auth:blacklist:" + jti;
    } // func end

    /* =========================================================
     *  Refresh Token 관리
     * ========================================================= */

    /**
     * Refresh Token 저장.
     *
     * @param refreshJti Refresh Token의 JTI
     * @param empId      사용자 사번(PK)
     * @param ttlSeconds TTL(토큰 유효 시간, 초 단위)
     *
     * <p>
     * Redis에 <code>auth:refresh:{jti}</code> → empId 형태로 저장되며,
     * TTL 종료 시 자동 삭제된다.
     * </p>
     */
    public void storeRefresh(String refreshJti, long empId, long ttlSeconds) {
        redisTemplate.opsForValue().set(refreshKey(refreshJti), empId, Duration.ofSeconds(ttlSeconds));
    } // func end

    /**
     * Refresh Token 존재 여부 확인.
     * DB 조회 없이 Redis를 통해 빠르게 확인 가능.
     */
    public boolean existsRefresh(String refreshJti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(refreshKey(refreshJti)));
    } // func end

    /**
     * Refresh Token 삭제 (로그아웃/재발급 시 호출).
     */
    public void deleteRefresh(String refreshJti) {
        redisTemplate.delete(refreshKey(refreshJti));
    } // func end

    /* =========================================================
     *  Access Token Blacklist 관리 (옵션)
     * ========================================================= */

    /**
     * Access Token을 블랙리스트 처리.
     *
     * <p>
     * JWT는 stateless라 기본적으로 토큰을 강제 만료시킬 수 없지만,<br>
     * 로그아웃 시 해당 JTI를 블랙리스트에 등록하면
     * 서버가 토큰을 거부할 수 있다.
     * </p>
     *
     * @param accessJti  Access Token의 JTI
     * @param ttlSeconds Access Token의 남은 수명(초)
     */
    public void blacklistAccess(String accessJti, long ttlSeconds) {
        redisTemplate.opsForValue()
                .set(blacklistKey(accessJti), true, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Access Token이 블랙리스트에 포함되어 있는지 확인.
     */
    public boolean isBlacklisted(String accessJti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(accessJti)));
    }
} // class end
