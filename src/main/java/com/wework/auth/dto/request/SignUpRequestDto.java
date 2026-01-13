package com.wework.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SignUpRequestDto {
    @NotBlank
    @Size(min = 4, max = 50)
    private String loginId;     // 로그인 ID
    
    @NotBlank
    @Size(min = 8, max=64)
    private String password;    // 비밀번호

    @NotBlank
    @Size(max = 50)
    private String name;        // 이름

    @Email
    @NotBlank
    @Size(max = 255)
    private String email;       // 이메일

    private LocalDate birthday; // 생일

    @Pattern(regexp = "M|F|O")
    private String sex;         // 성별

    private long deptId;        // 부서 Id(FK)

    @NotBlank
    @Pattern(regexp = "사원|과장|부장|이사|전무|대표")
    private String position;    // 직급

} // class end
