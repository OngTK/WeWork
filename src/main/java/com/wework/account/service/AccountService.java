package com.wework.account.service;

import com.wework.account.dto.response.MyAccountResponseDto;
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


} // class end
