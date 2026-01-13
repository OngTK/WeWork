package com.wework.auth.service;

import com.wework.auth.dto.request.SignUpRequestDto;
import com.wework.auth.dto.response.SignUpResponseDto;
import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_EMAIL_VERIFIED = "email_verified:";

    /**
     * [AUTH_001] 회원가입
     * */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto dto){
        // [1] 이메일 인증 여부 확인
        Object verified = redisTemplate.opsForValue().get(KEY_EMAIL_VERIFIED + dto.getEmail());
        if(verified == null) throw new IllegalStateException("Email not verified");

        // [2] loginId / email 중복 체크
        return null;
    } // func end

} // class end
