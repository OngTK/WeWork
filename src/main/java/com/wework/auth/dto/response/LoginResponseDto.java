package com.wework.auth.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken; // JWT 액세스 토큰
    private long expiresIn;     // 토큰 만료 시간 (단위 : sec)
    private UserSummary user;   // 로그인된 사용자 정보 요약

    /**
     * 로그인된 사용자 정보를 요약하여 담는 내부 DTO
     * 사번, 로그인 ID, 이름, 역할 목록을 포함
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class UserSummary{
        private long empId;         // 사번(PK)
        private String loginId;     // ID
        private String name;        // 이름 (추후 : employee에서 조회)
        private List<String> roles; // 역할 ROLE_XXX 문자열
    }
} // class end
