package com.wework.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateMyAccountRequestDto(

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50)
        String name,        // 이름

        @Email(message = "email 형식이 올바르지 않습니다.")
        @NotBlank(message = "email은 필수입니다.")
        @Size(max = 255)
        String email,       // 이메일

        LocalDate birthday, // 생일

        @Pattern(regexp = "M|F|O")
        String sex         // 성별

) { } // record end
