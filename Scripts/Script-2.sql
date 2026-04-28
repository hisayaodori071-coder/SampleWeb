USE sample_web;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS attendance_punches;
DROP TABLE IF EXISTS attendance_daily;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS holidays;
DROP TABLE IF EXISTS work_patterns;
DROP TABLE IF EXISTS employment_types;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS genders;
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE genders (
    gender_id BIGINT NOT NULL AUTO_INCREMENT,
    gender_name VARCHAR(20) NOT NULL,
    PRIMARY KEY (gender_id),
    UNIQUE KEY uk_genders_gender_name (gender_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE departments (
    department_id BIGINT NOT NULL AUTO_INCREMENT,
    department_code VARCHAR(20) NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (department_id),
    UNIQUE KEY uk_departments_department_code (department_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    role_id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(30) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    role_description VARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_roles_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE employment_types (
    employment_type_id BIGINT NOT NULL AUTO_INCREMENT,
    employment_type_code VARCHAR(20) NOT NULL,
    employment_type_name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (employment_type_id),
    UNIQUE KEY uk_employment_types_code (employment_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE work_patterns (
    work_pattern_id BIGINT NOT NULL AUTO_INCREMENT,
    work_pattern_code VARCHAR(20) NOT NULL,
    work_pattern_name VARCHAR(50) NOT NULL,
    scheduled_start_time TIME NULL,
    scheduled_end_time TIME NULL,
    standard_break_minutes SMALLINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (work_pattern_id),
    UNIQUE KEY uk_work_patterns_code (work_pattern_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id VARCHAR(20) NOT NULL,
    login_id VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name_kana VARCHAR(50) NOT NULL,
    first_name_kana VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    gender_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    employment_type_id BIGINT NOT NULL,
    user_status VARCHAR(20) NOT NULL,
    hire_date DATE NULL,
    retire_date DATE NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_employee_id (employee_id),
    UNIQUE KEY uk_users_login_id (login_id),
    UNIQUE KEY uk_users_email (email),
    CONSTRAINT fk_users_gender
        FOREIGN KEY (gender_id) REFERENCES genders (gender_id),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id),
    CONSTRAINT fk_users_employment_type
        FOREIGN KEY (employment_type_id) REFERENCES employment_types (employment_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_role_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (user_role_id),
    UNIQUE KEY uk_user_roles_user_role (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attendance_daily (
    attendance_daily_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    work_pattern_id BIGINT NULL,
    start_at DATETIME NULL,
    end_at DATETIME NULL,
    attendance_status VARCHAR(20) NOT NULL,
    remarks VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (attendance_daily_id),
    UNIQUE KEY uk_attendance_daily_user_work_date (user_id, work_date),
    CONSTRAINT fk_attendance_daily_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_attendance_daily_work_pattern
        FOREIGN KEY (work_pattern_id) REFERENCES work_patterns (work_pattern_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attendance_punches (
    attendance_punch_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    attendance_daily_id BIGINT NOT NULL,
    punch_type VARCHAR(20) NOT NULL,
    punched_at DATETIME NOT NULL,
    punch_method VARCHAR(20) NOT NULL,
    punch_note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (attendance_punch_id),
    KEY idx_attendance_punches_user_id (user_id),
    KEY idx_attendance_punches_attendance_daily_id (attendance_daily_id),
    KEY idx_attendance_punches_punched_at (punched_at),
    CONSTRAINT fk_attendance_punches_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_attendance_punches_daily
        FOREIGN KEY (attendance_daily_id) REFERENCES attendance_daily (attendance_daily_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE holidays (
    holiday_id BIGINT NOT NULL AUTO_INCREMENT,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (holiday_id),
    UNIQUE KEY uk_holidays_holiday_date (holiday_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO genders (gender_name) VALUES
('男'),
('女'),
('回答なし');

INSERT INTO departments (
    department_code,
    department_name,
    sort_order,
    deleted_flag,
    created_at,
    updated_at
) VALUES
('DEV', '開発部', 1, 0, NOW(), NOW()),
('HR', '人事部', 2, 0, NOW(), NOW());

INSERT INTO roles (
    role_code,
    role_name,
    role_description,
    sort_order,
    deleted_flag,
    created_at,
    updated_at
) VALUES
('ADMIN', '管理者', 'システム管理・ユーザー管理が可能', 1, 0, NOW(), NOW()),
('USER', '一般ユーザー', '自分の勤怠確認・打刻が可能', 2, 0, NOW(), NOW()),
('APPROVER', '承認者', '申請承認が可能', 3, 0, NOW(), NOW());

INSERT INTO employment_types (
    employment_type_code,
    employment_type_name,
    sort_order,
    deleted_flag,
    created_at,
    updated_at
) VALUES
('REGULAR', '正社員', 1, 0, NOW(), NOW()),
('CONTRACT', '契約社員', 2, 0, NOW(), NOW()),
('PARTTIME', 'アルバイト', 3, 0, NOW(), NOW());

INSERT INTO work_patterns (
    work_pattern_code,
    work_pattern_name,
    scheduled_start_time,
    scheduled_end_time,
    standard_break_minutes,
    sort_order,
    deleted_flag,
    created_at,
    updated_at
) VALUES
('NORMAL', '通常勤務', '09:00:00', '18:00:00', 60, 1, 0, NOW(), NOW()),
('EARLY', '早番', '08:00:00', '17:00:00', 60, 2, 0, NOW(), NOW()),
('SHORT', '短時間勤務', '09:00:00', '16:00:00', 45, 3, 0, NOW(), NOW());

INSERT INTO users (
    employee_id,
    login_id,
    password,
    last_name,
    first_name,
    last_name_kana,
    first_name_kana,
    email,
    gender_id,
    department_id,
    employment_type_id,
    user_status,
    hire_date,
    retire_date,
    sort_order,
    deleted_flag,
    updated_at
) VALUES
('E0001', 'user', 'pwd', '山田', '太郎', 'ヤマダ', 'タロウ', 'yamada@example.com', 1, 1, 1, 'ACTIVE', '2024-04-01', NULL, 1, 0, NOW()),
('E0002', 'sato', 'test123', '佐藤', '花子', 'サトウ', 'ハナコ', 'sato@example.com', 2, 2, 1, 'ACTIVE', '2023-04-01', NULL, 2, 0, NOW());

INSERT INTO user_roles (
    user_id,
    role_id,
    created_at,
    updated_at
) VALUES
(1, 1, NOW(), NOW()),
(1, 2, NOW(), NOW()),
(2, 2, NOW(), NOW());

SHOW TABLES;

SELECT * FROM genders;
SELECT * FROM departments;
SELECT * FROM roles;
SELECT * FROM employment_types;
SELECT * FROM work_patterns;
SELECT * FROM users;
SELECT * FROM user_roles;

USE sample_web;

SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS attendance_punches;
DROP TABLE IF EXISTS attendance_daily;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS holidays;
DROP TABLE IF EXISTS work_patterns;
DROP TABLE IF EXISTS employment_types;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS genders;
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS users;


SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE genders (
    gender_id BIGINT NOT NULL AUTO_INCREMENT,
    gender_name VARCHAR(20) NOT NULL,
    PRIMARY KEY (gender_id),
    UNIQUE KEY uk_genders_gender_name (gender_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE departments (
    department_id BIGINT NOT NULL AUTO_INCREMENT,
    department_code VARCHAR(20) NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (department_id),
    UNIQUE KEY uk_departments_department_code (department_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS departments;

CREATE TABLE departments (
    department_id BIGINT NOT NULL AUTO_INCREMENT,
    department_code VARCHAR(20) NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (department_id),
    UNIQUE KEY uk_departments_code (department_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    role_id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(30) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    role_description VARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_roles_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE employment_types (
    employment_type_id BIGINT NOT NULL AUTO_INCREMENT,
    employment_type_code VARCHAR(20) NOT NULL,
    employment_type_name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (employment_type_id),
    UNIQUE KEY uk_employment_types_code (employment_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE work_patterns (
    work_pattern_id BIGINT NOT NULL AUTO_INCREMENT,
    work_pattern_code VARCHAR(20) NOT NULL,
    work_pattern_name VARCHAR(50) NOT NULL,
    scheduled_start_time TIME NULL,
    scheduled_end_time TIME NULL,
    standard_break_minutes SMALLINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (work_pattern_id),
    UNIQUE KEY uk_work_patterns_code (work_pattern_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id VARCHAR(20) NOT NULL,
    login_id VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name_kana VARCHAR(50) NOT NULL,
    first_name_kana VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    gender_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    employment_type_id BIGINT NOT NULL,
    user_status VARCHAR(20) NOT NULL,
    hire_date DATE NULL,
    retire_date DATE NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_employee_id (employee_id),
    UNIQUE KEY uk_users_login_id (login_id),
    UNIQUE KEY uk_users_email (email),
    CONSTRAINT fk_users_gender
        FOREIGN KEY (gender_id) REFERENCES genders (gender_id),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id),
    CONSTRAINT fk_users_employment_type
        FOREIGN KEY (employment_type_id) REFERENCES employment_types (employment_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_role_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (user_role_id),
    UNIQUE KEY uk_user_roles_user_role (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attendance_daily (
    attendance_daily_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    work_pattern_id BIGINT NULL,
    start_at DATETIME NULL,
    end_at DATETIME NULL,
    attendance_status VARCHAR(20) NOT NULL,
    remarks VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (attendance_daily_id),
    UNIQUE KEY uk_attendance_daily_user_date (user_id, work_date),
    CONSTRAINT fk_attendance_daily_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_attendance_daily_work_pattern
        FOREIGN KEY (work_pattern_id) REFERENCES work_patterns (work_pattern_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attendance_punches (
    attendance_punch_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    attendance_daily_id BIGINT NOT NULL,
    punch_type VARCHAR(20) NOT NULL,
    punched_at DATETIME NOT NULL,
    punch_method VARCHAR(20) NOT NULL,
    punch_note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (attendance_punch_id),
    KEY idx_attendance_punches_user_punched_at (user_id, punched_at),
    KEY idx_attendance_punches_daily_punched_at (attendance_daily_id, punched_at),
    CONSTRAINT fk_attendance_punches_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_attendance_punches_daily
        FOREIGN KEY (attendance_daily_id) REFERENCES attendance_daily (attendance_daily_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE holidays (
    holiday_id BIGINT NOT NULL AUTO_INCREMENT,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (holiday_id),
    UNIQUE KEY uk_holidays_date (holiday_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO genders (gender_name) VALUES
('男'),
('女'),
('回答なし');

INSERT INTO departments (
    department_code, department_name, sort_order, deleted_flag, created_at, updated_at
) VALUES
('DEV', '開発部', 1, 0, NOW(), NOW()),
('HR', '人事部', 2, 0, NOW(), NOW());

INSERT INTO roles (
    role_code, role_name, role_description, sort_order, deleted_flag, created_at, updated_at
) VALUES
('ADMIN', '管理者', 'システム管理・ユーザー管理が可能', 1, 0, NOW(), NOW()),
('USER', '一般ユーザー', '自分の勤怠確認・打刻が可能', 2, 0, NOW(), NOW()),
('APPROVER', '承認者', '申請承認が可能', 3, 0, NOW(), NOW());

INSERT INTO employment_types (
    employment_type_code, employment_type_name, sort_order, deleted_flag, created_at, updated_at
) VALUES
('REGULAR', '正社員', 1, 0, NOW(), NOW()),
('CONTRACT', '契約社員', 2, 0, NOW(), NOW()),
('PARTTIME', 'アルバイト', 3, 0, NOW(), NOW());

INSERT INTO work_patterns (
    work_pattern_code, work_pattern_name,
    scheduled_start_time, scheduled_end_time, standard_break_minutes,
    sort_order, deleted_flag, created_at, updated_at
) VALUES
('NORMAL', '通常勤務', '09:00:00', '18:00:00', 60, 1, 0, NOW(), NOW()),
('EARLY', '早番', '08:00:00', '17:00:00', 60, 2, 0, NOW(), NOW()),
('SHORT', '短時間勤務', '09:00:00', '16:00:00', 45, 3, 0, NOW(), NOW());

INSERT INTO users (
    employee_id, login_id, password,
    last_name, first_name, last_name_kana, first_name_kana,
    email, gender_id, department_id, employment_type_id,
    user_status, hire_date, retire_date, sort_order, deleted_flag, updated_at
) VALUES
(
    'E0001', 'user', 'pwd',
    '山田', '太郎', 'ヤマダ', 'タロウ',
    'yamada@example.com', 1, 1, 1,
    'ACTIVE', '2024-04-01', NULL, 1, 0, NOW()
),
(
    'E0002', 'sato', 'test123',
    '佐藤', '花子', 'サトウ', 'ハナコ',
    'sato@example.com', 2, 2, 1,
    'ACTIVE', '2023-04-01', NULL, 2, 0, NOW()
);

INSERT INTO user_roles (user_id, role_id, created_at, updated_at) VALUES
(1, 1, NOW(), NOW()),
(1, 2, NOW(), NOW()),
(2, 2, NOW(), NOW());