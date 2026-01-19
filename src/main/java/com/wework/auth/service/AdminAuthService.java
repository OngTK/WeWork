package com.wework.auth.service;

import com.wework.auth.infra.redis.RedisTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final RedisTokenStore redisTokenStore;

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

} // class end
