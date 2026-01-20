package com.wework.auth.api;

import com.wework.auth.dto.request.PasswordResetRequestDto;
import com.wework.auth.service.PasswordRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password/reset")
@RequiredArgsConstructor
public class PasswordRestController {

    private final PasswordRestService passwordRestService;

    /**
     * [AUTH_030] 비밀번호 재설정 요청
     */
    @PostMapping
    public ResponseEntity<?> requestPasswordRest(@Valid @RequestBody PasswordResetRequestDto requestDto) throws NotFoundException {
        passwordRestService.requestPasswordRest(requestDto);
        return ResponseEntity.ok().build();
    } // func end

} // func end
