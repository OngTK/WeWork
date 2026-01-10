package com.wework.employee.repository;

import com.wework.employee.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    // [1] 로그인 아이디 찾기
    Optional<EmployeeEntity> findByLoginId(String loginId);

} // interface end
