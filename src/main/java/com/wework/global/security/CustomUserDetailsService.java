package com.wework.global.security;

import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.mapper.EmployeeAuthMapper;
import com.wework.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
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

     private final EmployeeRepository employeeRepository;
     private final EmployeeAuthMapper employeeAuthMapper;
//     private final AuthorizationQueryService authorizationQueryService;

    /**
     * loginId(사용자명)으로 사용자 정보를 로드.
     *
     * @param loginId 로그인 ID (폼에서 username으로 전달되는 값)
     * @return UserDetails 구현체(UserPrincipal)
     * @throws UsernameNotFoundException 사용자가 존재하지 않을 경우
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        // [1] loginId 기준으로 사용자 조회
        // - 존재하지 않으면 Spring Security 규약에 따라 UsernameNotFoundException 발생
        // - AuthenticationManager → DaoAuthenticationProvider → UserDetailsService 순으로 호출됨
        EmployeeEntity e = employeeRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + loginId));

        // [2] 비활성 상태 계정이면 로그인 차단
        // - e.isActive()는 status 필드가 ACTIVE 인지 검사
        // - DisabledException은 Spring Security가 처리하는 비활성 계정 예외
        if (!e.isActive()) {
            throw new DisabledException("Inactive user: " + loginId);
        }

        // [3] 계정의 역할(role) 목록을 조회
        // - 예: SUPER_ADMIN, MANAGER, WORKER
        // - 권한/인가 처리를 위해 ROLE_ 접두사를 붙여 GrantedAuthority 로 변환해야 함
        List<String> roleCodes = employeeAuthMapper.selectRoleCodesByEmpId(e.getEmpId());

        // [4] 스프링 시큐리티에서 사용하는 권한 객체(SimpleGrantedAuthority)로 변환
        // - ROLE_SUPER_ADMIN / ROLE_MANAGER / ROLE_WORKER 형태
        List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                .map(rc -> new SimpleGrantedAuthority("ROLE_" + rc))
                .toList();

        // [5] UserDetails 구현체(UserPrincipal)로 래핑하여 반환
        // - AuthenticationManager가 비밀번호 비교(BCrypt) 및 권한 검증에 사용
        // - enabled=true (비활성 여부는 이미 [2]에서 체크)
        return new UserPrincipal(
                e.getEmpId(),        // PK
                e.getLoginId(),      // username
                e.getPassword(),     // 인코딩된 bcrypt 비밀번호
                true,                // enabled (별도 disabled 조건 없음)
                authorities          // 권한 목록
        );
    } // func end

} // class end