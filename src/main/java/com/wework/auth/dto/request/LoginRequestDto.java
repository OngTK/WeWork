package com.wework.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank
    private String loginId;     // ID
    @NotBlank
    private String password;    // 비밀번호
} // class emd
