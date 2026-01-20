package com.wework.auth.dto.response;

public record PasswordResetOtpResponseDto(
        String resetToken,
        long expiresIn
) {
}
