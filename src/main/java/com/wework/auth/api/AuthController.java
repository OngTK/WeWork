package com.wework.auth.api;

import com.wework.auth.dto.request.LoginRequestDto;
import com.wework.auth.service.AuthService;
import com.wework.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * [AUTH_010] 로그인
     *
     * <p>처리 흐름</p>
     * <ul>
     *   <li>요청 Body(loginId, password)로 인증 수행</li>
     *   <li>성공 시 AccessToken은 응답 Body(JSON)로 반환</li>
     *   <li>RefreshToken은 보안을 위해 HttpOnly Cookie로 설정</li>
     * </ul>
     *
     * <p>RefreshToken을 Cookie로 내리는 이유</p>
     * <ul>
     *   <li>HttpOnly 설정으로 JS에서 접근 불가 → XSS 위험 감소</li>
     *   <li>재발급 API 호출 시 쿠키가 자동 전송되어 사용성이 좋음</li>
     * </ul>
     *
     * @param requestDto 로그인 요청 DTO (Validation 적용)
     * @param response   RefreshToken 쿠키 설정을 위한 HttpServletResponse
     * @return 로그인 성공 시 200 OK + AccessToken 포함 응답(JSON)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response){
        // [1] Service에 로그인 요청
        AuthService.LoginResult result = authService.login(requestDto);

        // [2] RefreshToken 쿠키 설정 (HttpOnly)
        //  - RefreshToken은 응답 Body에 포함하지 않고 쿠키로만 전달(보안상 권장)
        CookieUtil.addHttpOnlyCookie(
                response,
                "refreshToken",
                result.refreshToken(),
                result.refreshTtlSeconds(),
                false, // http = false / https = true
                "Lax",
                "/"
        );
        // [3] AccessToken은 JSON Body로 반환 (프론트는 Authorization 헤더에 실어 사용)
        return ResponseEntity.ok(result.body());
    } // func end

} // class end
