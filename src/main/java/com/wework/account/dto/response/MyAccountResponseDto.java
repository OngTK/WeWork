package com.wework.account.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyAccountResponseDto {

    private Long empId;             // 사번(PK)
    private String loginId;         // 로그인 ID
    private String name;            // 이름
    private List<String> roles;     // 역할 list

} // class end
