package com.wework.account.api;

import com.wework.account.service.AccountService;
import com.wework.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * [Account_001] 내 정보 조회
     * <p> @AuthenticationPrincipal
     * <p> - Spring Security에서 로그인한 사용자 정보를 컨트롤러 메서드 파라미터로 직접 주입
     * */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(accountService.getMyProfile(principal));
    } // func end

} // func end
