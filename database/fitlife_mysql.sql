-- 基于SpringBoot的健身房用户服务平台的设计与实现
-- MySQL 初始化脚本（v49 丰富演示数据版）
-- 运行：mysql -uroot -p < database/fitlife_mysql.sql

CREATE DATABASE IF NOT EXISTS fitlife_service
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE fitlife_service;

CREATE TABLE IF NOT EXISTS fitlife_state (
  state_key VARCHAR(64) PRIMARY KEY,
  state_json LONGTEXT NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 以下业务表由 Spring Boot 启动和接口写操作自动同步，便于查看丰富演示数据。
-- v49 使用新的 state_key：fitlife-v49-rich-database-state，首次启动会自动写入更丰富的会员、教练、课程、预约、积分商城、社区和教练邮箱数据。
CREATE TABLE IF NOT EXISTS fitlife_users (
  id BIGINT PRIMARY KEY,
  username VARCHAR(80),
  name VARCHAR(120),
  email VARCHAR(160),
  phone VARCHAR(60),
  role VARCHAR(40),
  active TINYINT(1),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_coaches (
  id BIGINT PRIMARY KEY,
  username VARCHAR(80),
  name VARCHAR(120),
  email VARCHAR(160),
  specialty VARCHAR(255),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_memberships (
  username VARCHAR(80) PRIMARY KEY,
  membership_type VARCHAR(80),
  end_date VARCHAR(40),
  status VARCHAR(40),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_classes (
  id BIGINT PRIMARY KEY,
  name VARCHAR(180),
  name_en VARCHAR(180),
  coach_name VARCHAR(120),
  type VARCHAR(40),
  start_time VARCHAR(40),
  end_time VARCHAR(40),
  capacity INT,
  booked_count INT,
  status VARCHAR(40),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_products (
  id BIGINT PRIMARY KEY,
  name VARCHAR(180),
  name_en VARCHAR(180),
  category VARCHAR(100),
  price DECIMAL(10,2),
  stock_quantity INT,
  active TINYINT(1),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_point_products (
  id BIGINT PRIMARY KEY,
  name VARCHAR(180),
  name_en VARCHAR(180),
  category VARCHAR(100),
  points_cost INT,
  stock_quantity INT,
  sold_count INT,
  active TINYINT(1),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_bookings (
  id BIGINT PRIMARY KEY,
  username VARCHAR(80),
  class_id BIGINT,
  status VARCHAR(40),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_posts (
  id BIGINT PRIMARY KEY,
  username VARCHAR(80),
  author VARCHAR(120),
  category VARCHAR(80),
  content TEXT,
  likes INT,
  comments INT,
  pinned TINYINT(1),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_teams (
  id BIGINT PRIMARY KEY,
  title VARCHAR(180),
  creator VARCHAR(80),
  current_members INT,
  max_members INT,
  joined TINYINT(1),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_points (
  username VARCHAR(80) PRIMARY KEY,
  available_points INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_exchanges (
  id BIGINT PRIMARY KEY,
  username VARCHAR(80),
  product_name VARCHAR(180),
  points_cost INT,
  status VARCHAR(40),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_facilities (
  id BIGINT PRIMARY KEY,
  name VARCHAR(180),
  name_en VARCHAR(180),
  available TINYINT(1),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_facility_bookings (
  id BIGINT PRIMARY KEY,
  facility_id BIGINT,
  facility_name VARCHAR(180),
  coach_username VARCHAR(80),
  booking_date VARCHAR(40),
  time_slot VARCHAR(60),
  status VARCHAR(40),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_complaints (
  id BIGINT PRIMARY KEY,
  username VARCHAR(80),
  user_name VARCHAR(120),
  coach_name VARCHAR(120),
  title VARCHAR(180),
  status VARCHAR(40),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitlife_reviews (
  id BIGINT PRIMARY KEY,
  coach_name VARCHAR(120),
  user_name VARCHAR(120),
  rating INT,
  class_name VARCHAR(180),
  responded TINYINT(1),
  raw_json LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
