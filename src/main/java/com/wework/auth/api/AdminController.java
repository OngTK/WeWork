package com.wework.auth.api;

import com.wework.auth.dto.request.ForceLogoutRequestDto;
import com.wework.auth.dto.request.LockAccountRequestDto;
import com.wework.auth.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminController {

    private final AdminAuthService adminAuthService;

    /**
     * [AUTH_013] 강제 로그아웃
     * */
    @PostMapping("/force-logout")
    public ResponseEntity<?> forceLogout(
            @Valid @RequestBody ForceLogoutRequestDto requestDto
            ){
        adminAuthService.forceLogout(requestDto.getEmpId());
        return ResponseEntity.ok().build();
    } // func end

    /**
     * [AUTH_034] 계정 잠금(퇴사) 처리
     * */
    @PostMapping("/lock")
    public ResponseEntity<?> lockAccount(@Valid @RequestBody LockAccountRequestDto requestDto){

    } // func end
} // class end
