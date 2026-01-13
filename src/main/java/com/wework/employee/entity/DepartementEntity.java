package com.wework.employee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepartementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;                // 부서ID (PK)

    @Column(name = "dept_name", nullable = false)
    private String deptName;            // 부서명

} // class end
