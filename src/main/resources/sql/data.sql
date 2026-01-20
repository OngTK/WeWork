-- ---------------------------------------------------------
-- 1) department
-- ---------------------------------------------------------
INSERT INTO department (dept_name) VALUES
('경영지원팀'),
('인사팀'),
('재무팀'),
('개발팀'),
('영업팀');

-- ---------------------------------------------------------
-- 2) role
-- ---------------------------------------------------------
INSERT INTO role (role_code, role_name) VALUES
('SUPER_ADMIN', 'Super Admin'),
('MANAGER', 'Manager'),
('WORKER', 'Worker');

-- ---------------------------------------------------------
-- 3) permission
-- ---------------------------------------------------------
INSERT INTO permission (perm_code, perm_name, description) VALUES
-- EMPLOYEE
('EMPLOYEE.CREATE', '직원 생성', '직원 정보 생성(입사 처리)'),
('EMPLOYEE.READ',   '직원 조회', '직원 정보 조회'),
('EMPLOYEE.UPDATE', '직원 수정', '직원 정보 수정(부서/직급/프로필 등)'),
('EMPLOYEE.DELETE', '직원 삭제', '직원 삭제(원칙적으로는 비활성화 권장)'),

-- ATTENDANCE
('ATTENDANCE.CREATE', '근태 생성', '근태 데이터 생성(출근/퇴근 기록 생성)'),
('ATTENDANCE.READ',   '근태 조회', '근태 데이터 조회'),
('ATTENDANCE.UPDATE', '근태 수정', '근태 데이터 수정(정정/수정)'),
('ATTENDANCE.DELETE', '근태 삭제', '근태 데이터 삭제'),

-- DOCUMENT (결재 문서)
('DOCUMENT.CREATE', '문서 생성', '결재 문서 작성/생성'),
('DOCUMENT.READ',   '문서 조회', '결재 문서 조회'),
('DOCUMENT.UPDATE', '문서 수정', '결재 문서 수정(임시저장/수정)'),
('DOCUMENT.DELETE', '문서 삭제', '결재 문서 삭제'),

-- RESOURCE (설비/예약)
('RESOURCE.CREATE', '설비/예약 생성', '설비 등록 또는 예약 생성(정책에 따라 구분 가능)'),
('RESOURCE.READ',   '설비/예약 조회', '설비/예약 조회'),
('RESOURCE.UPDATE', '설비/예약 수정', '설비/예약 수정'),
('RESOURCE.DELETE', '설비/예약 삭제', '설비/예약 삭제'),

-- DASHBOARD (통계/대시보드)
('DASHBOARD.CREATE', '대시보드 생성', '대시보드 위젯/구성 저장(옵션)'),
('DASHBOARD.READ',   '대시보드 조회', '대시보드 조회'),
('DASHBOARD.UPDATE', '대시보드 수정', '대시보드 설정 수정(옵션)'),
('DASHBOARD.DELETE', '대시보드 삭제', '대시보드 위젯/구성 삭제(옵션)');

-- ---------------------------------------------------------
-- 4) employee
-- ---------------------------------------------------------
INSERT INTO employee
(login_id, password, name, birthday, sex, email, status, dept_id, position)
VALUES
('admin', '$2a$10$EfZnMSBRBOjqInK2TgJSWuECPLti7jytBFYVSAj.pacE3Aq9B/Ani',   '최고관리자', '1985-02-14', 'M', 'admin@wework.local',   'ACTIVE', 200001, '대표'),
-- origin : {bcrypt}$2a$10$sampleAdminHash / password : 1234!
('mgr_hr',  '$2a$10$VbvNLQvaavuQ6PwpWDu.u.1VWkELNxSt9/NsYU.04w9TpmKzqyg7m',   '김지은',     '1990-06-01', 'F', 'ongtest3@gmail.com',   'ACTIVE', 200002, '부장'),
-- origin : {bcrypt}$2a$10$sampleMgrHash01 / password : testmgr!
('mgr_dev', '{bcrypt}$2a$10$sampleMgrHash02',   '박민수',     '1988-11-21', 'M', 'm.park@wework.local',  'ACTIVE', 200004, '이사'),
('mgr_sales','{bcrypt}$2a$10$sampleMgrHash03',  '이서준',     '1987-03-09', 'M', 's.lee@wework.local',   'ACTIVE', 200005, '부장'),

('wk_hr1',  '{bcrypt}$2a$10$0tGDrrozZchl.gTI7PKjf.Uf.tHbOdnRIsJU9c/es3SVMNmdD9jQW',    '정유진',     '1996-08-12', 'F', 'y.jung@wework.local',  'ACTIVE', 200002, '사원'),
-- origin : {bcrypt}$2a$10$sampleWkHash01 / password : wjddbwls01!
('wk_fin1', '{bcrypt}$2a$10$sampleWkHash02',    '한도윤',     '1994-12-30', 'M', 'd.han@wework.local',   'ACTIVE', 200003, '과장'),
('wk_dev1', '{bcrypt}$2a$10$sampleWkHash03',    '오태훈',     '1997-01-18', 'M', 't.oh@wework.local',    'ACTIVE', 200004, '사원'),
('wk_dev2', '{bcrypt}$2a$10$sampleWkHash04',    '서하린',     '1995-09-07', 'F', 'h.seo@wework.local',   'ACTIVE', 200004, '과장'),
('wk_sales1','{bcrypt}$2a$10$sampleWkHash05',   '윤지호',     '1993-05-22', 'M', 'j.yoon@wework.local',  'ACTIVE', 200005, '사원'),
('wk_left', '{bcrypt}$2a$10$sampleWkHash06',    '퇴사자',     '1992-10-10', 'O', 'left@wework.local',    'INACTIVE',200001,'사원');

-- ---------------------------------------------------------
-- 5) employee_role
-- role_id 가정: SUPER_ADMIN=300001, MANAGER=300002, WORKER=300003
-- ---------------------------------------------------------
INSERT INTO employee_role (emp_id, role_id) VALUES
(100001, 300001),
(100001, 300002),
(100001, 300003),
(100002, 300002),
(100002, 300003),
(100003, 300002),
(100003, 300003),
(100004, 300002),
(100004, 300003),
(100005, 300003),
(100006, 300003),
(100007, 300003),
(100008, 300003),
(100009, 300003);

-- ---------------------------------------------------------
-- 6) role_permission (각 role에 permission 매핑)
--  - SUPER_ADMIN: 전부
--  - MANAGER: 부서 범위에서 직원/근태/문서 일부 + 대시보드
--  - WORKER: 문서 작성/조회, 예약, 대시보드 조회 등
-- ---------------------------------------------------------
-- perm_id 가정: 400001부터 위 INSERT 순서대로 증가
-- 400001 EMPLOYEE.CREATE
-- 400002 EMPLOYEE.DISABLE
-- 400003 EMPLOYEE.UPDATE_ROLE
-- 400004 EMPLOYEE.UPDATE_POSITION
-- 400005 EMPLOYEE.READ
-- 400006 ATTENDANCE.READ
-- 400007 ATTENDANCE.MANAGE
-- 400008 DOCUMENT.READ
-- 400009 DOCUMENT.CREATE
-- 400010 DOCUMENT.APPROVE
-- 400011 RESOURCE.RESERVE
-- 400012 DASHBOARD.READ

-- SUPER_ADMIN(300001): 모든 permission
INSERT INTO role_permission (role_id, perm_id)
SELECT 300001, perm_id FROM permission;

-- MANAGER(300002): 부서 운영에 필요한 권한(예시: EMPLOYEE/ATTENDANCE/DOCUMENT/DASHBOARD 전체 CRUD)
INSERT INTO role_permission (role_id, perm_id)
SELECT 300002, perm_id
FROM permission
WHERE perm_code LIKE 'EMPLOYEE.%'
   OR perm_code LIKE 'ATTENDANCE.%'
   OR perm_code LIKE 'DOCUMENT.%'
   OR perm_code LIKE 'DASHBOARD.%';

-- WORKER(300003): 일반 사용 권한(예시: DOCUMENT CRUD + RESOURCE CRUD + DASHBOARD READ)
INSERT INTO role_permission (role_id, perm_id)
SELECT 300003, perm_id
FROM permission
WHERE perm_code LIKE 'DOCUMENT.%'
   OR perm_code LIKE 'RESOURCE.%'
   OR perm_code = 'DASHBOARD.READ';

-- ---------------------------------------------------------
-- 7) manager_scope
-- - 각 Manager가 관리하는 dept 지정
-- ---------------------------------------------------------
INSERT INTO manager_scope (manager_emp_id, dept_id) VALUES
(100002, 200002),  -- HR manager -> 인사팀
(100003, 200004),  -- Dev manager -> 개발팀
(100004, 200005),  -- Sales manager -> 영업팀
(100003, 200001),  -- (예시) Dev manager가 경영지원팀도 겸임
(100004, 200003);  -- (예시) Sales manager가 재무팀 일부도 관리
