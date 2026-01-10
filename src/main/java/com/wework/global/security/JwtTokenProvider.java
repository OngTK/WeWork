package com.wework.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 생성·검증·파싱을 담당하는 Provider 클래스.
 *
 * <p>핵심 기능:
 * <ul>
 *   <li>Access Token 생성</li>
 *   <li>JWT 서명 검증 및 Payload 파싱</li>
 *   <li>Token 유효성 검사</li>
 *   <li>empId / loginId 추출</li>
 * </ul>
 *
 * <p>JwtProperties에서 secret 및 만료 시간을 주입받아
 * HS256 기반 HMAC 서명키를 내부적으로 초기화한다.
 */
@Component
public class JwtTokenProvider {

    /** JWT 설정값(secret, expire time 등) */
    private final JwtProperties props;
    /** JWT 서명/검증용 Secret Key */
    private final Key key;

    /**
     * 생성자
     * <p>JwtProperties.secret 값을 바탕으로 HS256 서명키를 생성한다.
     */
    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    } // func end

    /**
     * Token의 메타 정보를 반환하기 위한 record
     * */
    public record TokenWithMeta(String token, String jti, long ttlSeconds) {}


    /**
     * AccessToken 생성.
     *
     * @param empId    사번(주체 subject 값으로 저장)
     * @param loginId  로그인 ID (claims 에 포함)
     * @return 서명된 JWT 문자열
     *
     * <p>Payload 정보:
     * <ul>
     *     <li>sub : empId</li>
     *     <li>loginId : 사용자 로그인 ID</li>
     *     <li>iat : 발급 시각</li>
     *     <li>exp : 만료 시각</li>
     * </ul>
     */
    public TokenWithMeta createAccessToken(long empId, String loginId) {
        // [1] UUID 생성
        String jti = UUID.randomUUID().toString();
        // [2] 현재 시각(UTC)
        Instant now = Instant.now();
        // [3] Access Token 만료 시각 계산
        //    - props.accessExpSeconds(): yml에서 설정한 만료 시간(초 단위)
        Instant exp = now.plusSeconds(props.accessExpSeconds());

        // [4] token 생성
        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(empId))      // JWT 표준 claim: sub (인증 주체 ID)
                .claims(Map.of(
                        "loginId", loginId,          // 커스텀 claim: 로그인 ID
                        "typ", "access"               // 커스텀 claim: 토큰 타입
                ))
                .issuedAt(Date.from(now))             // JWT 표준 claim: iat
                .expiration(Date.from(exp))           // JWT 표준 claim: exp
                .signWith(key)                        // HMAC-SHA256 서명
                .compact();                           // JWT 문자열 생성

        // [5] 토큰 + 메타 정보 반환
        return new TokenWithMeta(token, jti, props.accessExpSeconds());
    } // func end

    /**
     * 사용자 인증을 위한 Refresh Token(JWT)을 생성한다.
     *
     * <p>
     * Refresh Token은 Access Token이 만료되었을 때 새로운 Access Token을
     * 재발급받기 위한 용도로 사용되며, API 요청 인증에는 사용되지 않는다.
     * </p>
     *
     * <p>
     * 본 메서드에서 생성되는 Refresh Token은 다음 특징을 가진다.
     * </p>
     * <ul>
     *   <li>JWT 표준 claim {@code sub} 에 사용자 식별자(empId)를 저장한다.</li>
     *   <li>JWT ID({@code jti})를 UUID로 생성하여 토큰을 고유하게 식별한다.</li>
     *   <li>{@code typ=refresh} 커스텀 claim을 포함하여 Access Token과 구분한다.</li>
     *   <li>Access Token보다 긴 유효기간을 가진다.</li>
     * </ul>
     *
     * <p>
     * 생성된 Refresh Token은 일반적으로 다음과 같이 사용된다.
     * </p>
     * <ul>
     *   <li>HttpOnly Cookie에 저장하여 클라이언트 스크립트 접근을 차단한다.</li>
     *   <li>Redis에 {@code jti} 기준으로 저장하여 재발급 및 무효화 여부를 검증한다.</li>
     *   <li>로그아웃 또는 강제 로그아웃 시 Redis에서 삭제하여 즉시 무효화한다.</li>
     * </ul>
     *
     * @param empId   사용자 고유 식별자 (employee ID)
     * @param loginId 사용자 로그인 ID
     * @return 생성된 Refresh Token 문자열과 토큰 메타 정보(jti, TTL)를 포함한 {@link TokenWithMeta}
     */
    public TokenWithMeta createRefreshToken(long empId, String loginId) {
        // [1] JWT ID(JTI) 생성 - 토큰 고유 식별자
        String jti = UUID.randomUUID().toString();
        // [2] 현재 시각 (UTC 기준)
        Instant now = Instant.now();
        // [3] Refresh Token 만료 시각 계산
        Instant exp = now.plusSeconds(props.refreshExpSeconds());
        // [4] Refresh Token JWT 생성
        String token = Jwts.builder()
                .id(jti)                                   // jti: JWT ID
                .subject(String.valueOf(empId))            // sub: 사용자 식별자
                .claims(Map.of(
                        "loginId", loginId,                // 커스텀 claim: 로그인 ID
                        "typ", "refresh"                   // 커스텀 claim: 토큰 타입
                ))
                .issuedAt(Date.from(now))                  // iat: 발급 시간
                .expiration(Date.from(exp))                // exp: 만료 시간
                .signWith(key)                             // 서명 (HS256)
                .compact();
        // [5] 토큰과 메타 정보 반환
        return new TokenWithMeta(token, jti, props.refreshExpSeconds());
    } // func end

    /**
     * JWT 문자열을 파싱하고 서명 검증까지 수행.
     *
     * @param token  JWT 문자열
     * @return Jws<Claims> (Header + Claims + Signature)
     * @throws JwtException  서명 오류, 토큰 변조, 만료 등 오류 발생 시
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                // [1] SecretKey 기반 서명 검증 설정
                //  - 토큰이 서버에서 발급된 것인지 확인
                //  - 위·변조된 토큰은 이 단계에서 JwtException 발생
                .verifyWith((javax.crypto.SecretKey) key)
                // [2] 파서 생성
                .build()
                // [3] 서명된 JWT 파싱 및 Claims 추출
                //  - exp(만료), nbf(미래 사용), iat 등의 표준 검증 포함
                //  - 검증 실패 시 JwtException 발생
                .parseSignedClaims(token);
    } // func end

    /**
     * 토큰 유효성 검증.
     *
     * @param token JWT 문자열
     * @return true(정상), false(만료·서명불일치·구조오류 등)
     */
    public boolean validate(String token) {
        try {
            // [1] JWT 파싱 및 서명/만료 검증 수행
            //  - 유효한 토큰이면 예외 없이 통과
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // [2] JwtException:
            //     - 서명 검증 실패
            //     - 만료(exp)
            //     - 변조된 토큰
            //     IllegalArgumentException:
            //     - null 또는 비정상 문자열 전달
            //  → 인증 실패 여부만 필요하므로 예외는 삼키고 false 반환
            return false;
        }
    } // func end

    /**
     * 토큰에서 empId(subject) 추출.
     *
     * @param token JWT 문자열
     * @return empId(Long)
     */
    public long getEmpId(String token) {
        Claims claims = parse(token).getPayload();
        return Long.parseLong(claims.getSubject());
    } // func end

    /**
     * 토큰에서 loginId 추출.
     *
     * @param token JWT 문자열
     * @return loginId 또는 null
     */
    public String getLoginId(String token) {
        Claims claims = parse(token).getPayload();
        Object v = claims.get("loginId");
        return v == null ? null : String.valueOf(v);
    } // func end

    /**
     * 토큰의 typ(type) 값을 추출.
     *
     * <p>
     * typ는 Access Token / Refresh Token을 구분하기 위해
     * createAccessToken(), createRefreshToken() 생성 시
     * 커스텀 claim("typ")으로 저장해 둔 값이다.
     * </p>
     *
     * <p>
     * 예:
     * <ul>
     *   <li>Access Token → "Access"</li>
     *   <li>Refresh Token → "Refresh"</li>
     * </ul>
     * </p>
     *
     * @param token JWT 문자열
     * @return typ 값 ("AT" / "RT"), 없으면 null
     */
    public String getType(String token){
        // 커스텀 claim typ 읽기
        Object v = parse(token).getPayload().get("typ");
        return v == null ? null : String.valueOf(v);
    } // func end


    /**
     * JWT의 JTI(JWT ID)를 추출.
     *
     * <p>
     * jti는 JWT 표준 claim 중 하나이며,
     * 토큰 고유 식별값(Unique Identifier)을 나타낸다.
     * </p>
     *
     * <p>
     * 서버(또는 Redis)에서 Refresh Token 또는 Access Token을
     * 개별적으로 관리(무효화 / 재발급 / 블랙리스트 등록)할 때 사용한다.
     * </p>
     *
     * @param token JWT 문자열
     * @return jti 값(문자열)
     */
    public String getJti(String token){
        // Claims#getId() → 표준 claim "jti" 반환
        Claims claims = parse(token).getPayload();
        return claims.getId();
    } // func end
} // class end