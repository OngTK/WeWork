package com.wework.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security에서 사용하는 사용자 조회 서비스 구현체.
 *
 * <p>
 * 로그인 시 전달된 loginId(=username)를 기준으로<br>
 * DB에서 사용자 정보를 조회하고, {@link UserDetails} 형태의 객체로 반환한다.
 * </p>
 *
 * <p>
 * 실제 환경에서는 employee 테이블과 권한(역할) 정보를 조회하여<br>
 * {@link UserPrincipal} 을 구성하는 역할을 맡는다.
 * 현재는 로그인 플로우 확인을 위한 임시 하드코딩 버전이다.
 * </p>
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // 실제 구현 시 주입될 컴포넌트들 예시
    // private final EmployeeRepository employeeRepository;
    // private final AuthorizationQueryService authorizationQueryService;

    /**
     * loginId(사용자명)으로 사용자 정보를 로드.
     *
     * @param loginId 로그인 ID (폼에서 username으로 전달되는 값)
     * @return UserDetails 구현체(UserPrincipal)
     * @throws UsernameNotFoundException 사용자가 존재하지 않을 경우
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        /*
         * TODO: employee 테이블 조회 Repository/Mapper로 교체
         *
         * 실제 구현 예:
         *
         * Employee e = employeeRepository.findByLoginId(loginId)
         *     .orElseThrow(() -> new UsernameNotFoundException("User not found : " + loginId));
         *
         * boolean enabled = "ACTIVE".equals(e.getStatus());
         * List<String> roles = authorizationQueryService.getRoleCodes(e.getEmpId());
         *
         * List<SimpleGrantedAuthority> authorities = roles.stream()
         *     .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
         *     .toList();
         *
         * return new UserPrincipal(
         *     e.getEmpId(),
         *     e.getLoginId(),
         *     e.getPasswordHash(),
         *     enabled,
         *     authorities
         * );
         */

        // ============================
        // 임시 하드코딩(로그인 기능 뼈대 확인용)
        // ============================
        if (!"admin".equals(loginId)) {
            // 사용자가 없으면 UsernameNotFoundException을 던져야
            // Spring Security가 인증 실패로 처리한다.
            throw new UsernameNotFoundException("User not found: " + loginId);
        }

        long empId = 100001L;
        String passwordHash = "{bcrypt}$2a$10$sampleAdminHash"; // 실제 DB에서 읽어온 비밀번호 해시로 교체
        boolean enabled = true;

        // SUPER_ADMIN, MANAGER, WORKER 권한을 모두 가진 사용자 예시
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_WORKER")
        );

        // UserPrincipal은 UserDetails 구현체로서 SecurityContext에 저장될 인증 주체.
        return new UserPrincipal(empId, loginId, passwordHash, enabled, authorities);
    }

} // class end