-- Create database with UTF-8 support
CREATE DATABASE IF NOT EXISTS psychology_booking
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE psychology_booking;

-- User table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `nickname` VARCHAR(50),
    `real_name` VARCHAR(50),
    `phone` VARCHAR(20),
    `email` VARCHAR(100),
    `avatar` VARCHAR(255),
    `role` TINYINT DEFAULT 0 COMMENT '0-user, 1-counselor, 2-admin',
    `status` TINYINT DEFAULT 1 COMMENT '0-disabled, 1-enabled',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Counselor profile table
CREATE TABLE IF NOT EXISTS `counselor` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(50) COMMENT 'Professional title',
    `specialty` VARCHAR(255) COMMENT 'Specialization areas',
    `introduction` TEXT COMMENT 'Self introduction',
    `experience_years` INT DEFAULT 0,
    `price` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Price per session',
    `rating` DECIMAL(3,2) DEFAULT 5.00,
    `booking_count` INT DEFAULT 0,
    `available` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Booking table
CREATE TABLE IF NOT EXISTS `booking` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `counselor_id` BIGINT NOT NULL,
    `booking_date` DATE NOT NULL,
    `time_slot` VARCHAR(20) NOT NULL COMMENT 'e.g., 09:00-10:00',
    `status` TINYINT DEFAULT 0 COMMENT '0-pending, 1-confirmed, 2-completed, 3-cancelled',
    `notes` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`counselor_id`) REFERENCES `counselor`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Time slot table (咨询师可预约时间段)
CREATE TABLE IF NOT EXISTS `time_slot` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `counselor_id` BIGINT NOT NULL,
    `date` DATE NOT NULL,
    `start_time` VARCHAR(10) NOT NULL COMMENT 'HH:mm',
    `end_time` VARCHAR(10) NOT NULL COMMENT 'HH:mm',
    `status` TINYINT DEFAULT 0 COMMENT '0-available, 1-booked, 2-unavailable',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`counselor_id`) REFERENCES `counselor`(`id`),
    UNIQUE KEY `uk_counselor_date_time` (`counselor_id`, `date`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Review table (咨询评价)
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `booking_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `counselor_id` BIGINT NOT NULL,
    `rating` DECIMAL(2,1) NOT NULL COMMENT '1.0-5.0',
    `content` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`booking_id`) REFERENCES `booking`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`counselor_id`) REFERENCES `counselor`(`id`),
    UNIQUE KEY `uk_booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Payment table (支付记录)
CREATE TABLE IF NOT EXISTS `payment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `booking_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `order_no` VARCHAR(50) NOT NULL UNIQUE,
    `amount` DECIMAL(10,2) NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0-pending, 1-paid, 2-refunded, 3-failed',
    `payment_method` VARCHAR(20) COMMENT 'alipay, wechat, mock',
    `transaction_id` VARCHAR(100),
    `paid_at` DATETIME,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`booking_id`) REFERENCES `booking`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert test data (password: 123456)
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
('admin', '$2a$10$laUwQvESlkkqLF6T/8k9qOAO81w4oqiu5x1Pbb6SEnyGLL7Rr1h5W', '管理员', '13800000000', 2, 1),
('counselor', '$2a$10$laUwQvESlkkqLF6T/8k9qOAO81w4oqiu5x1Pbb6SEnyGLL7Rr1h5W', '张医生', '13800000001', 1, 1),
('counselor2', '$2a$10$laUwQvESlkkqLF6T/8k9qOAO81w4oqiu5x1Pbb6SEnyGLL7Rr1h5W', '李医生', '13800000003', 1, 1),
('counselor3', '$2a$10$laUwQvESlkkqLF6T/8k9qOAO81w4oqiu5x1Pbb6SEnyGLL7Rr1h5W', '王医生', '13800000004', 1, 1),
('user', '$2a$10$laUwQvESlkkqLF6T/8k9qOAO81w4oqiu5x1Pbb6SEnyGLL7Rr1h5W', '测试用户', '13800000002', 0, 1);

INSERT INTO `counselor` (`user_id`, `title`, `specialty`, `introduction`, `experience_years`, `price`, `rating`) VALUES
(2, '资深心理咨询师', '焦虑症,抑郁症,人际关系', '国家二级心理咨询师，从业10年，擅长认知行为疗法，帮助数千名来访者走出心理困境。', 10, 300.00, 4.9),
(3, '婚姻家庭咨询师', '婚姻情感,亲子关系,家庭治疗', '专注婚姻家庭咨询8年，国家认证婚姻家庭咨询师，帮助众多家庭重建幸福。', 8, 280.00, 4.8),
(4, '青少年心理专家', '青少年心理,学业压力,成长困惑', '儿童青少年心理咨询专家，擅长沙盘治疗、绘画治疗，深受青少年及家长信赖。', 6, 260.00, 4.7);

-- Insert sample time slots for counselors (next 7 days)
INSERT INTO `time_slot` (`counselor_id`, `date`, `start_time`, `end_time`, `status`) VALUES
(1, CURDATE(), '09:00', '10:00', 0),
(1, CURDATE(), '10:00', '11:00', 0),
(1, CURDATE(), '14:00', '15:00', 0),
(1, CURDATE(), '15:00', '16:00', 0),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00', '10:00', 0),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00', '11:00', 0),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00', '15:00', 0),
(2, CURDATE(), '09:00', '10:00', 0),
(2, CURDATE(), '10:00', '11:00', 0),
(2, CURDATE(), '14:00', '15:00', 0),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00', '10:00', 0),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00', '15:00', 0),
(3, CURDATE(), '10:00', '11:00', 0),
(3, CURDATE(), '15:00', '16:00', 0),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00', '11:00', 0),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00', '16:00', 0);
