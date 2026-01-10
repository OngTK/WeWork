package com.wework.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "employee")
public class EmployeeEntity {

    @Id
    @Column(name = "emp_id")
    private long empId;         // 사번(PK)

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;     // 로그인 ID

    @Column(name = "password", nullable = false)
    private String password;    // 비밀번호

    @Column(name = "name", nullable = false)
    private String name;        // 이름

    @Column(name = "status", nullable = false)
    private String status;      // 활성화 여부 ACTIVE/INACTIVE

    public boolean isActive(){
        // status가 "ACTIVE"와 대소문자 관계없이 일치하면 True
        return "ACTIVE".equalsIgnoreCase(status);
    } // func end

} // func end
