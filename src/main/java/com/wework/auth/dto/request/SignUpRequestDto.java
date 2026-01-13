package com.wework.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignUpRequestDto {
    @NotBlank
    private String loginId;     // 로그인 ID
    
    @NotBlank
    private String password;    // 비밀번호
    
    @Email
    @NotBlank
    private String email;       // 이메일
    
    @NotBlank
    private String name;        // 이름

} // class end
