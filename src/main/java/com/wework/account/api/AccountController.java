package com.wework.account.api;

import com.wework.account.dto.request.ChangePwRequestDto;
import com.wework.account.service.AccountService;
import com.wework.global.dto.response.CommonSuccessResponseDto;
import com.wework.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * [ACCOUNT_001] 내 정보 조회
     * <p> @AuthenticationPrincipal
     * <p> - Spring Security에서 로그인한 사용자 정보를 컨트롤러 메서드 파라미터로 직접 주입
     * */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(accountService.getMyProfile(principal));
    } // func end

    /**
     * [ACCOUNT_003] 비밀번호 변경
     * */
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePwRequestDto requestDto
            ){
        long empId = principal.getEmpId();
        accountService.changeMyPassword(empId,requestDto);
        return ResponseEntity.ok(CommonSuccessResponseDto.ok());
    } // func end

    /**
     * [ACCOUNT_004] 내 권한/역할/스코프 조회
     * */
    @GetMapping("/me/auth")
    public ResponseEntity<?> getMyAuth(@AuthenticationPrincipal UserPrincipal principal){
        long empId = principal.getEmpId();
        return ResponseEntity.ok(accountService.getMyAuth(empId));
    } // func end


} // func end
