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
    public JwtTokenProvider(JwtProperties props, Key key) {
        this.props = props;
        // key 파라미터는 사용하지 않고 props.secret()으로 실제 Key 생성
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    } // func end

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
    public String createAccessToken(long empId, String loginId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessExpSeconds());

        return Jwts.builder()
                .subject(String.valueOf(empId))   // JWT 표준 claim: sub
                .claims(Map.of(
                        "loginId", loginId
                ))
                .issuedAt(Date.from(now))         // iat
                .expiration(Date.from(exp))       // exp
                .signWith(key)                    // HS256 서명
                .compact();
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
                .verifyWith((javax.crypto.SecretKey) key)  // SecretKey 기반 서명 검증
                .build()
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
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
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
} // class end