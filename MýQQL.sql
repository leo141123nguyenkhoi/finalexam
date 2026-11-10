
CREATE DATABASE IF NOT EXISTS final_exam
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE final_exam;

DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS departments;

CREATE TABLE departments (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(30) NOT NULL,
  created_date DATETIME(6) NOT NULL,
  updated_date DATETIME(6) NOT NULL,
  CONSTRAINT uq_department_name UNIQUE (name),
  INDEX idx_department_type (type),
  INDEX idx_department_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE accounts (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(40) NOT NULL,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  email VARCHAR(190) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT FALSE,
  department_id BIGINT NULL,
  created_date DATETIME(6) NOT NULL,
  updated_date DATETIME(6) NOT NULL,
  CONSTRAINT uq_account_username UNIQUE (username),
  CONSTRAINT uq_account_email UNIQUE (email),
  CONSTRAINT fk_account_department FOREIGN KEY (department_id)
    REFERENCES departments(id) ON DELETE SET NULL,
  INDEX idx_account_created_date (created_date),
  INDEX idx_account_role (role),
  INDEX idx_account_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE password_reset_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  token_hash CHAR(64) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_reset_token_hash UNIQUE (token_hash),
  CONSTRAINT fk_reset_account FOREIGN KEY (account_id)
    REFERENCES accounts(id) ON DELETE CASCADE,
  INDEX idx_reset_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============== BANG 1: 13 PHONG BAN ===========================
INSERT INTO departments (id,name,type,created_date,updated_date) VALUES
  (1,'Development','DEV','2026-05-24 09:00:00','2026-05-24 09:00:00'),
  (2,'Testing','TEST','2026-06-02 09:00:00','2026-06-02 09:00:00'),
  (3,'Project Management','PM','2026-06-11 09:00:00','2026-06-11 09:00:00'),
  (4,'Scrum Master','SCRUM_MASTER','2026-06-20 09:00:00','2026-06-20 09:00:00'),
  (5,'IT Support','OTHER','2026-06-29 09:00:00','2026-06-29 09:00:00'),
  (6,'Human Resources','OTHER','2026-07-08 09:00:00','2026-07-08 09:00:00'),
  (7,'Sales','OTHER','2026-07-17 09:00:00','2026-07-17 09:00:00'),
  (8,'Marketing','OTHER','2026-07-26 09:00:00','2026-07-26 09:00:00'),
  (9,'Finance','OTHER','2026-08-04 09:00:00','2026-08-04 09:00:00'),
  (10,'Academy','OTHER','2026-08-13 09:00:00','2026-08-13 09:00:00'),
  (11,'Operations','OTHER','2026-08-22 09:00:00','2026-08-22 09:00:00'),
  (12,'Customer Success','OTHER','2026-08-31 09:00:00','2026-08-31 09:00:00'),
  (13,'Research & Development','DEV','2026-09-09 09:00:00','2026-09-09 09:00:00');

-- =============== BANG 2: 27 TAI KHOAN ===========================
INSERT INTO accounts
  (id,username,first_name,last_name,email,password_hash,role,active,department_id,created_date,updated_date) VALUES
  (1,'admin','System','Admin','admin@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','ADMIN',1,1,'2026-07-05 10:00:00','2026-07-05 10:00:00'),
  (2,'manager','Mai','Manager','manager@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','MANAGER',1,3,'2026-07-08 11:00:00','2026-07-08 11:00:00'),
  (3,'employee','An','Employee','employee@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,2,'2026-07-11 12:00:00','2026-07-11 12:00:00'),
  (4,'inactive','Binh','Pending','inactive@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',0,NULL,'2026-07-14 13:00:00','2026-07-14 13:00:00'),
  (5,'user01','Minh','Nguyen','user01@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,2,'2026-07-17 14:00:00','2026-07-17 14:00:00'),
  (6,'user02','Lan','Tran','user02@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,3,'2026-07-20 09:00:00','2026-07-20 09:00:00'),
  (7,'user03','Thao','Le','user03@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,4,'2026-07-23 10:00:00','2026-07-23 10:00:00'),
  (8,'user04','Duy','Pham','user04@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,5,'2026-07-26 11:00:00','2026-07-26 11:00:00'),
  (9,'user05','Tuan','Vo','user05@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,6,'2026-07-29 12:00:00','2026-07-29 12:00:00'),
  (10,'user06','Hoa','Hoang','user06@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,7,'2026-08-01 13:00:00','2026-08-01 13:00:00'),
  (11,'user07','Trang','Bui','user07@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','MANAGER',1,8,'2026-08-04 14:00:00','2026-08-04 14:00:00'),
  (12,'user08','Hieu','Do','user08@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,9,'2026-08-07 09:00:00','2026-08-07 09:00:00'),
  (13,'user09','Nam','Dang','user09@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,10,'2026-08-10 10:00:00','2026-08-10 10:00:00'),
  (14,'user10','Linh','Phan','user10@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,11,'2026-08-13 11:00:00','2026-08-13 11:00:00'),
  (15,'user11','My','Nguyen','user11@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,12,'2026-08-16 12:00:00','2026-08-16 12:00:00'),
  (16,'user12','Phuc','Tran','user12@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,13,'2026-08-19 13:00:00','2026-08-19 13:00:00'),
  (17,'user13','Khanh','Le','user13@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,1,'2026-08-22 14:00:00','2026-08-22 14:00:00'),
  (18,'user14','Ngoc','Pham','user14@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','MANAGER',1,2,'2026-08-25 09:00:00','2026-08-25 09:00:00'),
  (19,'user15','Long','Vo','user15@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,3,'2026-08-28 10:00:00','2026-08-28 10:00:00'),
  (20,'user16','Nhi','Hoang','user16@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,4,'2026-08-31 11:00:00','2026-08-31 11:00:00'),
  (21,'user17','Huy','Bui','user17@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,5,'2026-09-03 12:00:00','2026-09-03 12:00:00'),
  (22,'user18','Mai','Do','user18@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,6,'2026-09-06 13:00:00','2026-09-06 13:00:00'),
  (23,'user19','Bao','Dang','user19@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,7,'2026-09-09 14:00:00','2026-09-09 14:00:00'),
  (24,'user20','Ngan','Phan','user20@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,8,'2026-09-12 09:00:00','2026-09-12 09:00:00'),
  (25,'user21','Quang','Nguyen','user21@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','MANAGER',1,9,'2026-09-15 10:00:00','2026-09-15 10:00:00'),
  (26,'user22','Vy','Tran','user22@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,10,'2026-09-18 11:00:00','2026-09-18 11:00:00'),
  (27,'user23','Tung','Le','user23@finalexam.local','$2a$12$kUw6mBsIppnGNRyzCJVB2uHB0YmZikZi2y4wySDpIU346a36QanIW','EMPLOYEE',1,11,'2026-09-21 12:00:00','2026-09-21 12:00:00');

-- ================ KIEM TRA KET QUA ==============================
SELECT COUNT(*) AS so_department FROM departments; -- 13
SELECT COUNT(*) AS so_account FROM accounts; -- 27
SELECT role, COUNT(*) AS total FROM accounts GROUP BY role;
SELECT d.id, d.name, d.type, COUNT(a.id) AS total_member
FROM departments d LEFT JOIN accounts a ON a.department_id = d.id
GROUP BY d.id,d.name,d.type ORDER BY total_member DESC, d.id;
SELECT username, role, active, department_id FROM accounts ORDER BY id LIMIT 27;
