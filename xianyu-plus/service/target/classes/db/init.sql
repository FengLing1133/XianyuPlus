-- ============================================
-- XianyuPlus 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS xianyu_plus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xianyu_plus;

-- --------------------------------------------
-- 用户表
-- --------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `role` TINYINT DEFAULT 0 COMMENT '0用户 1管理员',
    `status` TINYINT DEFAULT 0 COMMENT '0正常 1封禁',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------
-- 分类表
-- --------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- --------------------------------------------
-- 商品表
-- --------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10,2) NOT NULL,
    `original_price` DECIMAL(10,2) DEFAULT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `user_id` BIGINT NOT NULL,
    `condition` TINYINT DEFAULT 1 COMMENT '1全新 2几乎全新 3轻微使用 4明显痕迹',
    `status` TINYINT DEFAULT 1 COMMENT '1在售 2已售出 3已下架',
    `view_count` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- --------------------------------------------
-- 商品图片表
-- --------------------------------------------
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `url` VARCHAR(500) NOT NULL,
    `sort_order` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- --------------------------------------------
-- 收藏表
-- --------------------------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- --------------------------------------------
-- 订单表
-- --------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `buyer_id` BIGINT NOT NULL,
    `seller_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0待付款 1已付款 2已完成 3已取消',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- --------------------------------------------
-- 消息表
-- --------------------------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL,
    `receiver_id` BIGINT NOT NULL,
    `product_id` BIGINT DEFAULT NULL,
    `content` TEXT NOT NULL,
    `is_read` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- --------------------------------------------
-- 预置数据
-- --------------------------------------------

-- 分类数据
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort_order`) VALUES
(1,  '电子产品', 0, 1),
(2,  '书籍教材', 0, 2),
(3,  '生活用品', 0, 3),
(4,  '衣物鞋包', 0, 4),
(5,  '运动户外', 0, 5),
(6,  '其他',     0, 6),
(7,  '手机',     1, 1),
(8,  '电脑',     1, 2),
(9,  '平板',     1, 3),
(10, '耳机',     1, 4),
(11, '教材',     2, 1),
(12, '考研',     2, 2),
(13, '小说',     2, 3),
(14, '日用',     3, 1),
(15, '美妆',     3, 2),
(16, '电器',     3, 3);

-- 管理员账号: admin / admin123
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2b$12$GtyiB1MT0XcQ2bht/v59OuQv.3OddBHy/2G0c9bYuduuXf3pNuBe2', '管理员', 1);

-- 测试用户: test1 / 123456, test2 / 123456
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('test1', '$2b$12$erx.KQEdSG1oUxWsPFpPV.xctZbeo9MK/LAKKNE16JDkVYSua3R5m', '测试用户1', 0),
('test2', '$2b$12$erx.KQEdSG1oUxWsPFpPV.xctZbeo9MK/LAKKNE16JDkVYSua3R5m', '测试用户2', 0);

-- 商品预置数据
INSERT INTO `product` (`id`, `title`, `description`, `price`, `original_price`, `category_id`, `user_id`, `condition`, `status`, `view_count`) VALUES
(1001, 'iPhone 15 Pro 256G 自然钛', '九成新，无磕碰，电池健康度95%，配件齐全', 6500.00, 8999.00, 7, 2, 2, 1, 128),
(1002, 'MacBook Air M2 8+256G', '轻薄本，适合日常办公学习，成色很好', 7200.00, 9499.00, 8, 2, 2, 1, 96),
(1003, 'iPad Air 5 64G WiFi版', '考研结束转让，带Apple Pencil', 3200.00, 4799.00, 9, 3, 2, 1, 75),
(1004, 'AirPods Pro 2代', '使用半年，降噪效果好', 1200.00, 1899.00, 10, 3, 3, 1, 64),
(1005, '高等数学同济第七版上下册', '几乎全新，只翻过几次', 25.00, 68.00, 11, 2, 2, 1, 42),
(1006, '考研英语真题汇编', '包含近20年真题，有少量笔记标注', 35.00, 89.00, 12, 3, 3, 1, 38),
(1007, '三体全套三册', '刘慈欣经典科幻小说，品相好', 45.00, 93.00, 13, 2, 2, 1, 55),
(1008, '小米台灯Pro', '护眼台灯，可调色温亮度', 80.00, 169.00, 14, 3, 2, 1, 29);

-- 商品图片
INSERT INTO `product_image` (`product_id`, `url`, `sort_order`) VALUES
(1001, '/uploads/products/demo-iphone.jpg', 0),
(1002, '/uploads/products/demo-macbook.jpg', 0),
(1003, '/uploads/products/demo-ipad.jpg', 0),
(1004, '/uploads/products/demo-airpods.jpg', 0),
(1005, '/uploads/products/demo-math.jpg', 0),
(1006, '/uploads/products/demo-kaoyan.jpg', 0),
(1007, '/uploads/products/demo-santi.jpg', 0),
(1008, '/uploads/products/demo-lamp.jpg', 0);
