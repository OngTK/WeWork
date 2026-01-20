package com.wework.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 서버 주소
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        // 쿠키 설정
        config.setAllowCredentials(true);

        // 허용 메소드
        config.setAllowedMethods(List.of(
                "GET","POST","PUT","DELETE"
        ));

        // Front가 보낼 수 있는 헤더
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));

        // Front가 읽을 수 있게 노출할 헤더
        config.setExposedHeaders(List.of(
                "Set-Cookie"
        ));

        // preflight 캐시(seconds)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    } // func end
} // class end
