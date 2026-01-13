package com.wework.account.service;

import com.wework.account.dto.response.MyAccountResponseDto;
import com.wework.account.dto.response.MyAuthResponseDto;
import com.wework.account.mapper.AccountAuthMapper;
import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.repository.EmployeeRepository;
import com.wework.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final EmployeeRepository employeeRepository;
    private final AccountAuthMapper accountAuthMapper;

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
     * [ACCOUNT_004] 내 권한/역할/스코프 조회
     * */
    public MyAuthResponseDto getMyAuth(long empId){
        List<String> roles = accountAuthMapper.selectRoleCodes(empId);
        List<String> permissions = accountAuthMapper.selectPermissionCodes(empId);
        List<Long> scopeDeptIds = accountAuthMapper.selectScopeDeptIds(empId);

        return new MyAuthResponseDto(roles,permissions,scopeDeptIds);
    } // func end

} // class end
