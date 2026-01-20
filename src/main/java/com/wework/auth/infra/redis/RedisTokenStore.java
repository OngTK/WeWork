package com.wework.auth.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

    // [AUTH_013] 추가
    private static final String KEY_REFRESH_JTI = "refresh:";       // refresh: {jti}
    private static final String KEY_EMP_REFRESH = "emp_refresh:";   // emp_refresh: {empId}

    // access jti 추적용 (강제 로그아웃 시 사용)
    private static final String KEY_EMP_ACCESS = "emp_access:"; // emp_access:{empId} -> accessJti

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
     * Refresh Token 정보를 Redis에 저장합니다.
     *
     * @param refreshJti  Refresh Token의 고유 식별자(JTI)
     * @param empId       사용자 사번(PK)
     * @param ttlSeconds  토큰의 TTL(Time To Live, 초 단위)
     *
     * <p>
     * 아래 두 가지 Key를 Redis에 저장합니다:
     * <ul>
     *   <li><code>auth:refresh:{jti}</code> → empId
     *       <br/>· 특정 Refresh Token(JTI)이 어떤 사용자에게 속하는지 조회할 때 사용합니다.</li>
     *
     *   <li><code>auth:emp_refresh:{empId}</code> → jti
     *       <br/>· 특정 사용자의 최신 Refresh Token(JTI)을 찾을 때 사용합니다.</li>
     * </ul>
     *
     * 두 Key 모두 TTL이 만료되면 자동 삭제됩니다.
     * </p>
     */
    public void storeRefresh(String refreshJti, long empId, long ttlSeconds) {
        // refresh:{jti} -> empId
        redisTemplate.opsForValue().set(KEY_REFRESH_JTI + refreshJti, String.valueOf(empId), ttlSeconds, TimeUnit.SECONDS);

        // emp_refresh:{empId} -> jti
        redisTemplate.opsForValue().set(KEY_EMP_REFRESH + empId, refreshJti, ttlSeconds, TimeUnit.SECONDS);
    } // func end

    /**
     * Refresh Token 존재 여부 확인.
     * DB 조회 없이 Redis를 통해 빠르게 확인 가능.
     */
    public boolean existsRefresh(String refreshJti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_REFRESH_JTI + refreshJti));
    } // func end

    /**
     * Refresh Token 삭제 처리 메서드
     *
     * <p>
     * 주로 로그아웃 또는 Refresh Token 재발급 시 호출되며,
     * 다음 두 가지 Redis 데이터를 함께 제거합니다:
     * </p>
     *
     * <ul>
     *   <li><code>auth:refresh:{jti}</code> → empId
     *       <br/>· Refresh Token(JTI)과 사용자 사번 매핑 데이터</li>
     *
     *   <li><code>auth:emp_refresh:{empId}</code> → jti
     *       <br/>· 해당 사용자의 현재 Refresh Token(JTI)을 저장한 데이터</li>
     * </ul>
     *
     * <p>
     * 전달받은 JTI 기반으로 먼저 empId 를 조회한 뒤,
     * 관련된 모든 Refresh Token 정보를 안전하게 삭제합니다.
     * </p>
     *
     * @param refreshJti 삭제할 Refresh Token의 고유 식별자(JTI)
     */
    public void deleteRefresh(String refreshJti) {
        String empId = (String) redisTemplate.opsForValue().get(KEY_REFRESH_JTI+ refreshJti);
        redisTemplate.delete(KEY_REFRESH_JTI + refreshJti);
        if(empId != null){
            redisTemplate.delete(KEY_EMP_REFRESH + empId);
        }
    } // func end

    /**
     * [AUTH_013] 강제 로그아웃 처리.
     *
     * <p>
     * 특정 사용자(empId)에 대한 Refresh Token 정보를 모두 제거합니다.
     * 다음 두 가지 Redis Key를 정리하는 방식으로 강제 로그아웃을 수행합니다:
     * </p>
     *
     * <ul>
     *   <li><code>auth:emp_refresh:{empId}</code> → jti
     *       <br/>· 해당 사용자의 현재 Refresh Token(JTI)을 저장한 Key</li>
     *
     *   <li><code>auth:refresh:{jti}</code> → empId
     *       <br/>· JTI 기반으로 사용자 사번을 조회하는 Key</li>
     * </ul>
     *
     * <p>
     * 강제 로그아웃 시나리오:
     * <br/>1) empId로 현재 JTI 조회
     * <br/>2) empId 기반 Key 삭제
     * <br/>3) 조회된 JTI 기반 Refresh Token Key도 함께 삭제
     * </p>
     *
     * @param empId 강제 로그아웃시킬 사용자 사번(PK)
     */
    public void deleteRefreshByEmpId(long empId) {

        // [1] 사용자에게 매핑된 Refresh Token JTI 조회
        String jti = (String) redisTemplate.opsForValue()
                .get(KEY_EMP_REFRESH + empId);

        // [2] auth:emp_refresh:{empId} 삭제 (사용자의 현재 Refresh Token 정보 제거)
        redisTemplate.delete(KEY_EMP_REFRESH + empId);

        // [3] auth:refresh:{jti} 삭제 (JTI 기준 Refresh Token 자체 제거)
        if (jti != null) {
            redisTemplate.delete(KEY_REFRESH_JTI + jti); // ★ 버그 수정됨
        }
    } // func end

    /**
     * 로그인 시 AccessJti 를 redis에 저장
     * 관리자 계정은 empId를 가지고 일반 계정 로그아웃을 하기위해서는 별도의 토큰 정보를 확인할 방법이 필요
     * */
    public void storeAccessJti(long empId, String accessJti, long ttlSeconds) {
        redisTemplate.opsForValue().set(KEY_EMP_ACCESS + empId, accessJti, ttlSeconds, TimeUnit.SECONDS);
    } // func end

    /**
     * EmpId > accessJti를 찾아 블랙리스트에 등록
     * */
    public void blacklistAccessByEmpId(long empId) {

        String accessJti = (String) redisTemplate.opsForValue().get(KEY_EMP_ACCESS + empId);
        if (accessJti == null) return;

        Long ttl = redisTemplate.getExpire(KEY_EMP_ACCESS + empId, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) {
            // 만료 임박/만료면 블랙리스트 등록 의미 없음
            redisTemplate.delete(KEY_EMP_ACCESS + empId);
            return;
        }

        // 기존 구현 메서드 활용
        blacklistAccess(accessJti, ttl);

        // 추적 키 삭제(선택)
        redisTemplate.delete(KEY_EMP_ACCESS + empId);
    }

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

    // [AUTH_033] 로그인 실패 카운터 ==========
    private String loginFailKey(String loginId) {
        return "auth:login_fail:" + loginId;
    }

    /**
     * [AUTH_033] 로그인 실패 횟수 증가
     * - TTL을 같이 걸어 "일정 시간 후 자동 초기화"
     * - 예: 30분 동안 누적, 30분 지나면 자동 리셋
     *
     * @return 증가 후 실패 횟수
     */
    public long increaseLoginFail(String loginId, long ttlSeconds) {
        String key = loginFailKey(loginId);

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // 첫 실패일 때만 TTL 세팅
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return count == null ? 0 : count;
    }

    /** 로그인 성공 시 실패 횟수 초기화(권장: AUTH_010 내부) */
    public void clearLoginFail(String loginId) {
        redisTemplate.delete(loginFailKey(loginId));
    }

    /** (선택) 현재 실패 횟수 조회 */
    public long getLoginFailCount(String loginId) {
        Object v = redisTemplate.opsForValue().get(loginFailKey(loginId));
        if (v == null) return 0;
        try { return Long.parseLong(String.valueOf(v)); }
        catch (Exception e) { return 0; }
    }

    /* ===================================================================
    *  AUTH_030 비밀번호 재설정 OTP > Redis 저장
    *  =================================================================== */
    private  String pwRestKey(String loginId){
        return "auth:pw_reset:" + loginId ;
    } // func end

    public void storePwRestOtp(String loginId, String otp, long ttlSecondes){
        redisTemplate.opsForValue().set(pwRestKey(loginId), otp, ttlSecondes, TimeUnit.SECONDS);
    } // func end

    public String getPwRestOtp(String loginId){
        Object v = redisTemplate.opsForValue().get(pwRestKey(loginId));
        return v == null ? null : String.valueOf(v);
    } // func end

    public void deletePwRestOtp(String loginId){
        redisTemplate.delete(pwRestKey(loginId));
    } // func end

    /* ===================================================================
     *  AUTH_031~032 비밀번호 재설정 OTP 인증 후
     *  Token > redis 저장
     *  =================================================================== */
    public String pwRestTokenKey(String loginId){
        return "auth:pw_reset_token:" + loginId;
    } // func end

    public void storePwStoreToken(String loginId, String resetToken, long ttlSeconds){
        redisTemplate.opsForValue().set(pwRestTokenKey(loginId),resetToken,ttlSeconds,TimeUnit.SECONDS);
    } // func end

    public String getPwResetToken(String loginId){
        Object v = redisTemplate.opsForValue().get(pwRestTokenKey(loginId));
        return v == null ? null : String.valueOf(v);
    } // func end

    public void deletePwResetToken(String loginId){
        redisTemplate.delete(pwRestTokenKey(loginId));
    } // func end
    
} // class end
