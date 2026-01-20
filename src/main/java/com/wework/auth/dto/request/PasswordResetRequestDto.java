package com.wework.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDto(
        @NotBlank(message = "loginId는 필수입니다.")
        String loginId
){
}
