package com.wework.auth.api;

import com.wework.auth.dto.request.EmailCodeSendRequestDto;
import com.wework.auth.dto.request.EmailCodeVerifyRequestDto;
import com.wework.auth.dto.request.LoginRequestDto;
import com.wework.auth.dto.request.SignUpRequestDto;
import com.wework.auth.dto.response.SignUpResponseDto;
import com.wework.auth.service.AuthService;
import com.wework.auth.service.EmailAuthCodeService;
import com.wework.auth.service.SignUpService;
import com.wework.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailAuthCodeService emailAuthCodeService;
    private final SignUpService signUpService;

    // 회원가입 관련 =============================================================
    /**
     * [AUTH_001] 회원가입
     * */
    @PostMapping("/signup")
    public ResponseEntity<?> singUp(@Valid @RequestBody SignUpRequestDto dto){
        SignUpResponseDto response = signUpService.signUp(dto);
        return ResponseEntity.ok(response);
    } // func end

    /**
     * [AUTH_002] 이메일 인증코드 발송
     * */
    @PostMapping("/email")
    public ResponseEntity<?> sendEmailCode(@Valid @RequestBody EmailCodeSendRequestDto requestDto){
        emailAuthCodeService.sendCode(requestDto.getEmail());
        return ResponseEntity.ok().build();
    } // func end

    /**
     * [AUTH_003] 이메일 인증코드 검증
     * */
    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmailCode(@Valid @RequestBody EmailCodeVerifyRequestDto requestDto){
        boolean result = emailAuthCodeService.verifyCode(requestDto.getEmail(), requestDto.getCode());
        // false 이면, 400
        if(!result){
            return ResponseEntity.status(400).body("Invalid Code");
        }
        return ResponseEntity.ok().build();
    } // func end

    // 로그인·토큰 관련 ===========================================================

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

    /**
     * [AUTH_011] 로그아웃
     * - RefreshToken 쿠키 삭제
     * - Redis Refresh 세션 삭제
     * */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletResponse response,
            HttpServletRequest request
    ){
        // [1] Cookie에서 refreshToken 조회
        String refreshToken = CookieUtil.getCookieValue(request,"refreshToken")
                .orElse(null);

        // [2] Service 처리 (Redis refresh 삭제)
        authService.logout(refreshToken);

        // [3] Client 쿠키 삭제
        CookieUtil.deleteCookie(
                response,
                "refreshToken",
                false,
                "Lax",
                "/"
        );
        return ResponseEntity.ok().build();
    } // func end

    /**
     * [AUTH_012] 토큰 재발급 (Reissue Token API)
     *
     * <p>HTTP Only Cookie에 저장된 Refresh Token 기반으로
     * <p>새로운 Access Token을 재발급(reissue)하는 엔드포인트.
     *
     * <p>✔ 클라이언트는 Request 헤더가 아닌,
     * <p>  Cookie(refreshToken)로 Refresh Token을 전송한다.
     * <p>✔ 서버에서는 Redis에 저장된 Refresh Token과 비교해 유효성 검증을 수행한다.
     * <p>✔ (권장 보안패턴) Refresh Rotation 적용:
     * <p>    - Refresh Token을 사용할 때마다 새 RT를 재발급하고
     * <p>      이전 RT는 Redis에서 제거 또는 블랙리스트 처리.
     *
     */
    @PostMapping("/token")
    public ResponseEntity<?> reissueToken(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        // [1] Refresh Token을 HttpOnly Cookie에서 추출
        // - 쿠키가 없거나 이름이 다르면 IllegalArgumentException 발생
        String refreshToken = CookieUtil.getCookieValue(request, "refreshToken")
                .orElseThrow(() -> new IllegalArgumentException("Refresh cookie not found"));

        // [2] 토큰 재발급 처리
        // - refreshToken 유효성 검증 (Redis 조회)
        // - Refresh Rotation 적용 시 새 RT 및 AT 생성
        // - 재발급 AccessToken + 새 RefreshToken + 만료시간 반환
        AuthService.ReissueResult result = authService.reissueToken(refreshToken);

        // [3] 새 Refresh Token으로 쿠키 갱신
        // - HttpOnly + Secure(false: local) + SameSite=Lax
        // - 운영 환경에서는 secure=true, SameSite=None 권장
        CookieUtil.addHttpOnlyCookie(
                response,
                "refreshToken",
                result.refreshToken(),      // 재발급된 Refresh Token
                result.refreshTtlSeconds(), // 새 TTL
                false,                      // 로컬 환경이므로 secure=false
                "Lax",
                "/"
        );

        // [4] Access Token은 JSON Body로 반환
        // - 프론트는 Authorization 헤더로 설정하여 사용
        return ResponseEntity.ok(result.body());
    } // func end

} // class end
