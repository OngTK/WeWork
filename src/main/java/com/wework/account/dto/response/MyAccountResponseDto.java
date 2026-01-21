package com.wework.account.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.swing.plaf.BorderUIResource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MyAccountResponseDto {

    private Long empId;             // 사번(PK)
    private String loginId;         // 로그인 ID
    private String name;            // 이름
    private List<String> roles;     // 역할 list

    private LocalDate birthday;     // 생일
    private String email;           // 이메일
    private String position;        // 직급
    private String deptName;        // 부서

} // class end
