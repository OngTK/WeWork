package com.wework.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EmailCodeVerifyRequestDto {
    @Email
    @NotBlank
    private String email;       // 확인 대상 이메일

    @NotNull
    private String code;        // 검증코드
} // class end
