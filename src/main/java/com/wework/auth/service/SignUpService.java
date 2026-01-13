package com.wework.auth.service;

import com.wework.auth.dto.request.SignUpRequestDto;
import com.wework.auth.dto.response.SignUpResponseDto;
import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.repository.DepartmentRepository;
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
    private final DepartmentRepository departmentRepository;
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
        if(employeeRepository.existsByEmail(dto.getEmail())) throw new IllegalStateException("Email already exist");
        if(employeeRepository.existsByLoginId(dto.getLoginId())) throw new IllegalStateException("LoginId already exist");

        // [3] deptId 유효성 체크
        if(!departmentRepository.existsByDeptId(dto.getDeptId())) throw new IllegalStateException("Department ID not found: " + dto.getDeptId());

        // [4] 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(dto.getPassword());

        // [5] Employee Entity 생성
        EmployeeEntity employee = EmployeeEntity.builder()
                .loginId(dto.getLoginId())
                .password(encodedPw)
                .name(dto.getName())
                .birthday(dto.getBirthday())
                .sex(dto.getSex())
                .email(dto.getEmail())
                .deptId(dto.getDeptId())
                .position(dto.getPosition())
                .status("ACTIVE") // 서버 고정
                .build();

        // [6] Entity 저장
        employeeRepository.save(employee);

        // [7] Redis 인증 플래스 삭제
        redisTemplate.delete(KEY_EMAIL_VERIFIED+dto.getEmail());

        // [8] 결과 반환
        return new SignUpResponseDto(employee.getEmpId(), employee.getLoginId());

    } // func end

} // class end
