-- 0) DB 생성/선택
-- CREATE DATABASE IF NOT EXISTS WeWork
--   DEFAULT CHARACTER SET utf8mb4
--   DEFAULT COLLATE utf8mb4_0900_ai_ci;
-- USE WeWork;
-- SET NAMES utf8mb4;

-- FK 때문에 삭제 순서 중요
DROP TABLE IF EXISTS manager_scope;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS employee_role;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS department;

-- =========================================================
-- 1) Department (부서 테이블)
-- =========================================================
CREATE TABLE department (
  dept_id     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  dept_name   VARCHAR(255) NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_department PRIMARY KEY (dept_id),
  CONSTRAINT uk_department_dept_name UNIQUE (dept_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  AUTO_INCREMENT=200001;

-- =========================================================
-- 2) Role (역할 테이블)
-- =========================================================
CREATE TABLE role (
  role_id     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_code   VARCHAR(30)  NOT NULL,
  role_name   VARCHAR(50)  NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_role PRIMARY KEY (role_id),
  CONSTRAINT uk_role_role_code UNIQUE (role_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  AUTO_INCREMENT=300001;

-- =========================================================
-- 3) Employee (직원 테이블)
-- =========================================================
CREATE TABLE employee (
  emp_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  login_id   VARCHAR(50)  NOT NULL UNIQUE,
  password   VARCHAR(255) NOT NULL,
  name       VARCHAR(50)  NOT NULL,
  birthday   DATE NULL,
  sex        CHAR(1) NULL,
  email      VARCHAR(255) NULL UNIQUE,
  status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  dept_id    INT UNSIGNED NULL,
  position   VARCHAR(10) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_employee PRIMARY KEY (emp_id),
  CONSTRAINT uk_employee_login_id UNIQUE (login_id),
  -- 부서 FK (Role 시트: ON UPDATE CASCADE / ON DELETE SET NULL)
  CONSTRAINT fk_employee_dept
    FOREIGN KEY (dept_id) REFERENCES department(dept_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,

  CONSTRAINT ck_employee_sex
    CHECK (sex IN ('M','F','O') OR sex IS NULL),
  CONSTRAINT ck_employee_status
    CHECK (status IN ('ACTIVE','INACTIVE')),
  CONSTRAINT ck_employee_position
    CHECK (position IN ('사원','과장','부장','이사','전무','대표'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  AUTO_INCREMENT=100001;

CREATE INDEX idx_employee_dept_id ON employee(dept_id);

-- =========================================================
-- 4) employee_role (직원-역할 연결 / PK(emp_id, role_id))
-- =========================================================
CREATE TABLE employee_role (
  emp_id     BIGINT UNSIGNED NOT NULL,
  role_id    INT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_employee_role PRIMARY KEY (emp_id, role_id),
  CONSTRAINT fk_employee_role_emp
    FOREIGN KEY (emp_id) REFERENCES employee(emp_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_employee_role_role
    FOREIGN KEY (role_id) REFERENCES role(role_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_employee_role_role_id ON employee_role(role_id);

-- =========================================================
-- 5) permission (접근 권한 테이블)
-- =========================================================
CREATE TABLE permission (
  perm_id     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  perm_code   VARCHAR(100) NOT NULL,
  perm_name   VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_permission PRIMARY KEY (perm_id),
  CONSTRAINT uk_permission_perm_code UNIQUE (perm_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  AUTO_INCREMENT=400001;

-- =========================================================
-- 6) role_permission (역할-권한 연결 / PK(role_id, perm_id))
-- =========================================================
CREATE TABLE role_permission (
  role_id    INT UNSIGNED NOT NULL,
  perm_id    INT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_role_permission PRIMARY KEY (role_id, perm_id),

  CONSTRAINT fk_role_permission_role
    FOREIGN KEY (role_id) REFERENCES role(role_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_role_permission_perm
    FOREIGN KEY (perm_id) REFERENCES permission(perm_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_role_permission_perm_id ON role_permission(perm_id);

-- =========================================================
-- 7) manager_scope (관리자-스코프 / PK(manager_emp_id, dept_id))
-- =========================================================
CREATE TABLE manager_scope (
  manager_emp_id BIGINT UNSIGNED NOT NULL,
  dept_id        INT UNSIGNED NOT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_manager_scope PRIMARY KEY (manager_emp_id, dept_id),

  CONSTRAINT fk_manager_scope_emp
    FOREIGN KEY (manager_emp_id) REFERENCES employee(emp_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_manager_scope_dept
    FOREIGN KEY (dept_id) REFERENCES department(dept_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_manager_scope_dept_id ON manager_scope(dept_id);

