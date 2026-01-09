package com.wework.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 관련 설정값을 외부 설정파일(yml)에서 바인딩하기 위한 Properties 클래스
 * <p>application.yml 내 다음과 같은 구조의 값을 읽어온다:</p>
 * <pre>
 * security:
 *   jwt:
 *     secret: "JWT_SECRET_KEY"
 *     access-exp-seconds: 3600
 *     refresh-exp-seconds: 1209600
 * </pre>\
 * <p>
 * Spring Boot의 {@link ConfigurationProperties} 를 이용하여<br>
 * - JWT 서명용 secret 키<br>
 * - Access Token 만료 시간(초)<br>
 * - Refresh Token 만료 시간(초)<br>
 * 등의 값을 자동으로 주입받는다.
 * </p>
 *
 * <p>
 * record를 사용하여 불변 객체로 구성되며,<br>
 * 설정값을 생성자 인자를 통해 안전하게 주입받게 된다.
 * </p>
 * */

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        /* JWT 서명/검증에 사용되는 Secret Key */
        String secret,
        /* Access Token의 만료 시간(초 단위) */
        long accessExpSeconds,
        /* Refresh Token의 만료 시간(초 단위) */
        long refreshExpSeconds
) {}
