package com.wework.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailAuthCodeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;

    private static final String KEY_EMAIL_VERIFY = "email_verify:"; // 검증 이메일
    private static final long CODE_TTL_SECONDS = 300;               // 검증 코드

    /**
     * 6자리 난수 만들기 함수
     * */
    private String generate6DigitalCode(){
        // 100000~999999 사이의 난수 발생
        int n = ThreadLocalRandom.current().nextInt(100000,1000000);
        return String.valueOf(n);
    } // func end

    /**
     * 매일 발송(Gmail SMTP)
     * */
    private void setMail(String email, String code){
        try{
            // [1] Message / Helper 객체 생성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            // [2] 송신주소 / 제목 / 내용(HTML여부) 설정
            helper.setTo(email);
            helper.setSubject("[WeWork] 이메일 인증코드");
            helper.setText("인증코드: " + code + "\n유효시간: " + (CODE_TTL_SECONDS/60) + "분",false);
            // [3] 이메일 발송
            mailSender.send(message);
        }catch (MessagingException e){
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    /**
     * [AUTH_02] 이메일 인증코드 발송
     * */
    public void sendCode(String email){
        // [1] 난수 코드 생성
        String code = generate6DigitalCode();
        // [2] Redis 저장(TTL)
        redisTemplate.opsForValue().set(KEY_EMAIL_VERIFY + email, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);
        // [3] Gmail SMTP 발송
        setMail(email,code);
    } // func end

    /**
     * [AUTH_03] 이메일 인증코드 검증
     * */
    public boolean verifyCode(String email, String code){
        // [1] Redis에서 Key : email 에 대해 저장된 code를 추출
        Object savedCode = redisTemplate.opsForValue().get(KEY_EMAIL_VERIFY+email);
        // [2] code 가 null 인지 확인
        if(savedCode == null) return false;
        // [3] Redis 코드와 사용자 입력 코드의 일치 확인
        boolean ok = String.valueOf(savedCode).equals(code);
        // [4] 일치할 경우, Redis에서 관련 정보 삭제
        if(ok){
            redisTemplate.delete(KEY_EMAIL_VERIFY+email);
        }
        return ok;
    } // func end

} // class end
