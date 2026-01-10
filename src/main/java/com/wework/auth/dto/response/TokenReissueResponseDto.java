package com.wework.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenReissueResponseDto {

    private String accessToken;     // 접큰 토큰
    private long expiresIn;         // 토큰 완료 시간 (secondes)

} // class end
