package com.wework.auth.service;

import com.wework.auth.infra.redis.RedisTokenStore;
import com.wework.employee.entity.EmployeeEntity;
import com.wework.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final RedisTokenStore redisTokenStore;
    private final EmployeeRepository employeeRepository;

    /**
     * [AUTH_013] 강제 로그아웃
     * - 대상 empId의 Refresh 세션을 Redis에서 삭제하여 재발급을 차단
     * @param empId 대상 사용자 사번
     * */
    public void forceLogout(long empId){
        // [1] 세션에 없더라도 예외 없이 refresh 삭제
        redisTokenStore.deleteRefreshByEmpId(empId);

        // [2] access 블랙리스트 등록
        redisTokenStore.blacklistAccessByEmpId(empId);
    } // func end

    /**
     * [AUTH_034] 계정 잠금(퇴사) 처리
     * */
    @Transactional
    public void lockAccount(long empId) throws NotFoundException {
        EmployeeEntity employeeEntity = employeeRepository.findById(empId)
                .orElseThrow(() -> new NotFoundException("해당 empId를 찾을 수 없습니다."));
        employeeEntity.setStatus("INACTIVE"); // 상태 비활성화 = 퇴사
    } // func end
} // class end
