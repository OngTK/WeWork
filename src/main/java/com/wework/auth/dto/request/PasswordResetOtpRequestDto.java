package com.wework.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetOtpRequestDto(
        @NotBlank(message = "loginId는 필수입니다.")
        String loginId,
        @NotBlank(message = "OTP는 필수입니다.")
        String otp
) {}
