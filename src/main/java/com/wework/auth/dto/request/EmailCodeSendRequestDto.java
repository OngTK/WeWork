package com.wework.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EmailCodeSendRequestDto {
    @Email
    @NotNull
    private String email;       // 발송할 이메일 경로    
} // class end
