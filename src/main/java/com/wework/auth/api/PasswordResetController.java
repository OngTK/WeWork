package com.wework.auth.api;

import com.wework.auth.dto.request.PasswordResetOtpRequestDto;
import com.wework.auth.dto.request.PasswordResetRequestDto;
import com.wework.auth.dto.response.PasswordResetOtpResponseDto;
import com.wework.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * [AUTH_030] 비밀번호 재설정 요청
     */
    @PostMapping
    public ResponseEntity<?> requestPasswordRest(@Valid @RequestBody PasswordResetRequestDto requestDto) throws NotFoundException {
        passwordResetService.requestPasswordRest(requestDto);
        return ResponseEntity.ok().build();
    } // func end

    /**
     * [AUTH_031] 비밀번호 재설정 인증코드 검증
     * */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyRestOtp(@Valid @RequestBody PasswordResetOtpRequestDto requestDto){
        return ResponseEntity.ok(passwordResetService.verifyResetOtp(requestDto));
    } // func end

} // func end
