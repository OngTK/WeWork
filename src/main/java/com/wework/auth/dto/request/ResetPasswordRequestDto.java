package com.wework.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank(message = "loginId는 필수입니다.")
        String loginId,

        @NotBlank(message = "resetToken은 필수입니다.")
        String resetToken,

        @NotBlank(message = "newPassword는 필수입니다.")
        @Size(min = 5, max = 30, message = "newPassword는 5~30자여야 합니다.")
        String newPassword
) {
}
