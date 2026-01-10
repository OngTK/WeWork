package com.wework.employee.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeAuthMapper {

    // [1] empId를 통해 해당 계정에 부여된 역할 정보를 List<String>으로 가져옴
    List<String> selectRoleCodesByEmpId(@Param("empId") long empId);

} // interface end
