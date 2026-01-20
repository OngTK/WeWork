package com.wework.auth.service;

import com.wework.auth.dto.request.PasswordResetRequestDto;
import com.wework.auth.infra.redis.RedisTokenStore;
import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.repository.EmployeeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PasswordRestService {

    private final EmployeeRepository employeeRepository;
    private final RedisTokenStore redisTokenStore;
    private final JavaMailSender mailSender;

    // todo 난수 + 이메일 발송 관련 해서 공통 클래스로 분리 필요
    /**
     * 6자리 난수 만들기 함수
     * */
    private String generateOtp6(){
        // 100000~999999 사이의 난수 발생
        int n = ThreadLocalRandom.current().nextInt(100000,1000000);
        return String.valueOf(n);
    } // func end

    /**
     * 메일 발송(Gmail SMTP)
     * */
    private void setMail(String email, String code, long ttlSecond){
        try{
            // [1] Message / Helper 객체 생성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            // [2] 송신주소 / 제목 / 내용(HTML여부) 설정
            helper.setTo(email);
            helper.setSubject("[WeWork] 이메일 인증코드");
            helper.setText("인증코드: " + code + "\n유효시간: " + (ttlSecond/60) + "분",false);
            // [3] 이메일 발송
            mailSender.send(message);
        }catch (MessagingException e){
            throw new IllegalStateException("Failed to send email", e);
        }
    }
    
    /**
     * [AUTH_030] 비밀번호 재설정 요청
     * - Login 로 계정 확인
     * - OTP 생성 + Redis 저장(TTL)
     * - 이메일 발송
     * */
    public void requestPasswordRest(PasswordResetRequestDto requestDto) throws NotFoundException {
        // [1] 회원 정보 조회
        EmployeeEntity employeeEntity = employeeRepository.findByLoginId(requestDto.loginId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 ID 입니다."));

        // [2] OTP 난수 생성
        String otp = generateOtp6();

        // [3] Redis 저장 (10min)
        redisTokenStore.storePwRestOtp(requestDto.loginId(), otp, 600);

        // [4] 이메일 발송
        setMail(employeeEntity.getEmail(),otp,600);
    } // func end

} // class end
