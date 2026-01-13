package com.wework.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "employee")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private long empId;         // 사번(PK)

    @Column(name = "login_id", nullable = false, length = 50, unique = true)
    private String loginId;     // 로그인 ID

    @Column(name = "password", nullable = false, length = 255)
    private String password;    // 비밀번호

    @Column(name = "name", nullable = false, length = 50)
    private String name;        // 이름

    @Column
    private LocalDate birthday; // 생일

    @Column(length = 1)
    private String sex;         // 성별 : M/F/O

    @Column(length = 255)
    private String email;       // 이메일

    @Column(name = "dept_id")
    private Long deptId;        // 부서Id(FK) FK는 DB에 존재 (서비스에서 유효성 체크)

    @Column(nullable = false, length = 10)
    private String position;    // 직급 : 사원/과장/부장/이사/전무/대표

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;        // 생성일

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;        // 수정일

    @Column(name = "status", nullable = false , length = 20)
    private String status;                  // 활성화 여부 ACTIVE/INACTIVE

    public boolean isActive(){
        // status가 "ACTIVE"와 대소문자 관계없이 일치하면 True
        return "ACTIVE".equalsIgnoreCase(status);
    } // func end

} // func end
