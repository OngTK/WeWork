package com.wework.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * [AUTH_013] 강제 로그아웃
 * */

@Getter
public class ForceLogoutRequestDto {
    @NotNull
    private long empId;     // 사번
} // class end
