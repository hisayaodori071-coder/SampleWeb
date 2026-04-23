CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    recorded_at DATETIME NOT NULL
);
SELECT * FROM attendance_records;
INSERT INTO attendance_records (login_id, action, recorded_at)
VALUES ('sato', '出勤', NOW());
SELECT * FROM attendance_records;
SHOW TABLES;
CREATE TABLE users (
    login_id VARCHAR(50) PRIMARY KEY,
    user_password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL
);
INSERT INTO users (login_id, user_password, display_name) VALUES
('user', 'pwd', '山田 太郎'),
('sato', 'test123', '佐藤 花子');
SELECT * FROM users;