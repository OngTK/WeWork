package com.wework.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 애플리케이션의 인증 주체(Principal)를 표현하는 클래스.
 *
 * <p>
 * Spring Security의 {@link UserDetails} 인터페이스를 구현하여<br>
 * 로그인 ID, 비밀번호, 권한(roles/permissions), 계정 활성화 여부 등을 제공한다.
 * </p>
 *
 * <p>
 * SecurityContextHolder에 저장되는 핵심 사용자 정보이며,<br>
 * JwtAuthenticationFilter → UserDetailsService → UserPrincipal 흐름에서 사용된다.
 * </p>
 */
@Getter
public class UserPrincipal implements UserDetails {

    /* =========================================================
     *  기본 사용자 속성
     * ========================================================= */

    private final long empId;           // 사번(PK)
    private final String loginId;       // 로그인 ID (Spring Security에서 username으로 사용됨)
    private final String password;      // 인코딩된 비밀번호(BCrypt 등)
    private final boolean enabled;      // 계정 활성화 여부(퇴사자, 비활성 계정 처리 등)
    private final Collection<? extends GrantedAuthority> authorities;   // 사용자 권한(ROLE_XXX) 목록

    /* =========================================================
     *  생성자
     * ========================================================= */

    /**
     * UserPrincipal 생성자.
     *
     * @param empId       사번
     * @param loginId     로그인 ID
     * @param password    암호화된 비밀번호
     * @param enabled     활성화 여부
     * @param authorities 권한 목록
     */
    public UserPrincipal(long empId, String loginId, String password, boolean enabled,
                         Collection<? extends GrantedAuthority> authorities) {
        this.empId = empId;
        this.loginId = loginId;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /* =========================================================
     *  UserDetails 인터페이스 구현
     * ========================================================= */

    /**
     * Spring Security에서 사용되는 username 필드.
     * 여기서는 loginId를 username으로 사용한다.
     */
    @Override
    public String getUsername() {
        return loginId;
    }

    /**
     * 계정 만료 여부.
     * 시스템 요구사항에 따라 만료 설정을 도입할 수 있으나,
     * 현재는 항상 true로 처리.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부.
     * 필요한 경우 계정 잠금 정책과 연결하여 변경 가능.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 인증 정보(비밀번호) 만료 여부.
     * 비밀번호 변경 정책 등과 연결할 수 있으나 현재는 항상 true.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부.
     * 퇴사자/정지 계정 등에 대해 false로 설정 가능.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
} // class end
