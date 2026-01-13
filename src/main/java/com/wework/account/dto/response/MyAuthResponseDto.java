package com.wework.account.dto.response;

import java.util.List;

/**
 * [Account_004] 내 권한/역할/스코프 조회 전용 DTO
 * */
public record MyAuthResponseDto(
        List<String> roles,
        List<String> permission,
        List<Long> scopeDeptIds
) {
}
