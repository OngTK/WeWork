package com.wework.employee.repository;

import com.wework.employee.entity.DepartementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartementEntity, Long> {

    // [1] 부서ID(PK) 존재여부 확인
    boolean existsByDeptId (Long deptId);
} // interface end
