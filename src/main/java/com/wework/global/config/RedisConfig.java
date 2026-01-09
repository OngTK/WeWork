package com.wework.global.config;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.temporal.Temporal;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){

        // [1] RedisTemplate 객체 생성
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // [2] Redis 연결 팩토리 설정
        template.setConnectionFactory(connectionFactory);

        // [3] Key·Value 직렬화를 위한 객체 선언
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        // [4] Key 직렬화 => 문자열 형태로 저장
        template.setKeySerializer(keySerializer);
        // [5] Value 직렬화 => JSON 형태로 변환
        template.setValueSerializer(valueSerializer);
        // [6] hash key 직렬화
        template.setHashKeySerializer(keySerializer);
        // [7] hash value 직렬화
        template.setHashValueSerializer(valueSerializer);

        // [8] 내부 속성 검증 및 serializer 적용 확정
        template.afterPropertiesSet();

        return template;
    } // func end

} // class end
