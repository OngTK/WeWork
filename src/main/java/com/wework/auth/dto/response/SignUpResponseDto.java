package com.wework.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponseDto {

    private long empId;         // 사번
    private String loginId;     // 로그인 Id
} // class end
