package com.wework.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePwRequestDto (
        @NotBlank(message = "oldPw는 필수입니다.")
        String oldPw,
        @NotBlank(message = "oldPw는 필수입니다.")
        @Size(min = 8, max = 64)
        // @Pattern 활성화하여 특수문자 사용 등 정책 활성화 가능
        String newPw
) { } // record end
