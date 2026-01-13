package com.wework.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountAuthMapper {

    // [ACCOUNT_004] 내 권한/역할/스코프 조회
    // 역할 리스트 조회
    List<String> selectRoleCodes(@Param("empId") long empId);
    // 권한 조회
    List<String> selectPermissionCodes(@Param("empId") long empId);
    // 스코프 조회
    List<Long> selectScopeDeptIds(@Param("empId") long empId);

} // interface end
