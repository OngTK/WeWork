package com.wework.auth.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LockAccountRequestDto(
    @NotNull(message = "empId는 필수입니다.")
    @Min(value = 100000 , message = "empId는 100000 이상입니다.")
    Long empId
) { } // class end
