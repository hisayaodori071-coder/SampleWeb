USE sample_web;

CREATE TABLE IF NOT EXISTS users_backup_20260423 AS
SELECT * FROM users;

CREATE TABLE IF NOT EXISTS attendance_records_backup_20260423 AS
SELECT * FROM attendance_records;

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
    UNIQUE KEY uk_roles_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

