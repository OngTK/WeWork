package com.wework.account.service;

import com.wework.account.dto.request.ChangePwRequestDto;
import com.wework.account.dto.response.MyAccountResponseDto;
import com.wework.account.dto.response.MyAuthResponseDto;
import com.wework.account.mapper.AccountAuthMapper;
import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.repository.EmployeeRepository;
import com.wework.global.exception.ForbiddenException;
import com.wework.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final EmployeeRepository employeeRepository;
    private final AccountAuthMapper accountAuthMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * [Account_001] 내 정보 조회
     * */
    public MyAccountResponseDto getMyProfile(UserPrincipal userPrincipal){

        // [1] 인증 주체의 empId로 직원 정보 조회
        EmployeeEntity employeeEntity = employeeRepository.findById(userPrincipal.getEmpId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Employee not found"));

        // [2] 권한 목록 추출
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map( a -> a.getAuthority())
                .toList();

        // [3] 응답 DTO 구성
        return MyAccountResponseDto.builder()
                .empId(employeeEntity.getEmpId())
                .loginId(employeeEntity.getLoginId())
                .name(employeeEntity.getName())
                .roles(roles)
                .build();
    } // [Account_001] func end
    
    /**
     * [ACCOUNT_003] 비밀번호 변경
     * */
    @Transactional
    public void changeMyPassword(long empId, ChangePwRequestDto request){
        // [1] JPA 로 기존 pw 꺼내오기
        EmployeeEntity employeeEntity = employeeRepository.findById(empId)
                .orElseThrow(() -> new ForbiddenException("유효하지 않은 사용자 입니다."));
        String currentHashPw = employeeEntity.getPassword();
        // [2] 기존 비밀번호 확인
        if(currentHashPw == null || !passwordEncoder.matches(request.oldPw(), currentHashPw)){
            throw new ForbiddenException("기존 비밀번호가 일치하지 않습니다.");
        }
        // [3] 동일 비밀번호로 변경 방지
        if(passwordEncoder.matches(request.newPw(),currentHashPw)){
            throw new ForbiddenException("기존 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
        }
        // [4] 새 비밀번호 hash 및 저장
        String newHashPw = passwordEncoder.encode(request.newPw());
        employeeEntity.setPassword(newHashPw);
    } // func end


    /**
     * [ACCOUNT_004] 내 권한/역할/스코프 조회
     * */
    public MyAuthResponseDto getMyAuth(long empId){
        List<String> roles = accountAuthMapper.selectRoleCodes(empId);
        List<String> permissions = accountAuthMapper.selectPermissionCodes(empId);
        List<Long> scopeDeptIds = accountAuthMapper.selectScopeDeptIds(empId);

        return new MyAuthResponseDto(roles,permissions,scopeDeptIds);
    } // func end

} // class end
