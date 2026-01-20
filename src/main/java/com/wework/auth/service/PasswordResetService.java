package com.wework.auth.service;

import com.wework.auth.dto.request.PasswordResetOtpRequestDto;
import com.wework.auth.dto.request.PasswordResetRequestDto;
import com.wework.auth.dto.request.ResetPasswordRequestDto;
import com.wework.auth.dto.response.PasswordResetOtpResponseDto;
import com.wework.auth.infra.redis.RedisTokenStore;
import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.repository.EmployeeRepository;
import com.wework.global.exception.ForbiddenException;
import com.wework.global.exception.UnauthorizedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final EmployeeRepository employeeRepository;
    private final RedisTokenStore redisTokenStore;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * [AUTH_031] 비밀번호 재설정 인증코드 검증
     * - OTP 일치 시 resetToken 발급 및 Redis
     * */
    public PasswordResetOtpResponseDto verifyResetOtp(PasswordResetOtpRequestDto requestDto){
        // [1] Redis에 저장된 OTP 조회
        String savedOtp = redisTokenStore.getPwRestOtp(requestDto.loginId());
        if(savedOtp == null) throw new UnauthorizedException("OTP가 존재하지 않거나 만료되었습니다.");
        // [2] OTP 비교
        if(!savedOtp.equals(requestDto.otp())) throw new UnauthorizedException("OTP가 일치하지 않습니다.");
        // [3] OTP 삭제
        redisTokenStore.deletePwRestOtp(requestDto.loginId());
        // [4] resetToken 발급 + Redis 저장
        String resetToken = generateResetToken();
        long ttlSeconds = 600; // 10min
        redisTokenStore.storePwStoreToken(requestDto.loginId(), resetToken, ttlSeconds);
        // [5] 결과 반환
        return new PasswordResetOtpResponseDto(resetToken, ttlSeconds);
    } // func end

    /**
     * [AUTH_031 내부 메소드] Token 발행용 내부 메소드
     * */
    private String generateResetToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    } // func end

    /**
     * [AUTH_032] 비밀번호 재설정
     * - resetToken 검증
     * - 새 비밀번호로 변경(BCrypt)
     * - resetToken 삭제(1회성)
     * - 보안: refresh 전부 무효화
     * */
    @Transactional
    public void resetPassword(ResetPasswordRequestDto requestDto) throws NotFoundException {
        // [1] redis에서 resetToken 조회·검증
        String savedToken = redisTokenStore.getPwResetToken(requestDto.loginId());
        if(savedToken == null) throw new ForbiddenException("재설정 토큰이 만료되었거나 존재하지 않습니다.");
        if(!savedToken.equals(requestDto.resetToken())) throw new ForbiddenException("재설정 토큰이 일치하지 않습니다.");
        // [2] JPA 사용자 정보 조회
        EmployeeEntity employee = employeeRepository.findByLoginId(requestDto.loginId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 입니다."));
        // [3] 새 비밀번호와 기존 비밀번호의 일치여부 판단
        if(passwordEncoder.matches(requestDto.newPassword(), employee.getPassword())){
            throw new ForbiddenException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
        }
        // [4] 비밀번호 업데이트
        String newPwd = passwordEncoder.encode(requestDto.newPassword());
        employee.setPassword(newPwd);
        // [5] resetToken 삭제
        redisTokenStore.deletePwResetToken(requestDto.loginId());
        // [6] 기존 refresh 전부 무효화
        redisTokenStore.deleteRefreshByEmpId(employee.getEmpId());
        // [7] 로그인 실패 카운트 초기화
        redisTokenStore.clearLoginFail(requestDto.loginId());
    } // func end
} // class end
