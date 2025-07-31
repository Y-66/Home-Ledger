-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ledger` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `ledger`;

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS `reminder_logs`;
DROP TABLE IF EXISTS `repayment_schedule`;
DROP TABLE IF EXISTS `loan_contracts`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `users`;

-- 创建用户表
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建客户表
CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `full_name` varchar(100) NOT NULL COMMENT '客户姓名',
  `id_number` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` text DEFAULT NULL COMMENT '地址',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`customer_id`),
  KEY `idx_full_name` (`full_name`),
  KEY `idx_contact_phone` (`contact_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 创建借贷合同表
CREATE TABLE `loan_contracts` (
  `contract_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `customer_id` int(11) NOT NULL COMMENT '客户ID',
  `loan_amount` decimal(15,2) NOT NULL COMMENT '借贷金额',
  `annual_interest_rate` decimal(5,2) NOT NULL COMMENT '年利率',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `interest_type` varchar(20) NOT NULL DEFAULT 'fixed' COMMENT '利息类型：fixed-固定，variable-浮动',
  `repayment_frequency` varchar(20) NOT NULL DEFAULT 'monthly' COMMENT '还款频率：monthly-月付，quarterly-季付，annually-年付',
  `contract_status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '合同状态：active-活跃，closed-关闭，default-违约',
  `principal_paid` decimal(15,2) DEFAULT 0.00 COMMENT '已还本金',
  `principal_payment_date` date DEFAULT NULL COMMENT '本金还款日期',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`contract_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_contract_status` (`contract_status`),
  KEY `idx_start_date` (`start_date`),
  FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借贷合同表';

-- 创建还款计划表
CREATE TABLE `repayment_schedule` (
  `schedule_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '还款计划ID',
  `contract_id` int(11) NOT NULL COMMENT '合同ID',
  `period_start` date NOT NULL COMMENT '计息开始日期',
  `period_end` date NOT NULL COMMENT '计息结束日期',
  `due_date` date NOT NULL COMMENT '到期日期',
  `calculated_interest` decimal(15,2) NOT NULL COMMENT '应付利息',
  `period_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待付，paid-已付，overdue-逾期',
  `payment_date` date DEFAULT NULL COMMENT '实际付款日期',
  `last_reminder_date` date DEFAULT NULL COMMENT '最后提醒日期',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`schedule_id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_due_date` (`due_date`),
  KEY `idx_period_status` (`period_status`),
  FOREIGN KEY (`contract_id`) REFERENCES `loan_contracts` (`contract_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款计划表';

-- 创建提醒日志表
CREATE TABLE `reminder_logs` (
  `reminder_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `schedule_id` int(11) DEFAULT NULL COMMENT '还款计划ID',
  `reminder_type` varchar(50) NOT NULL COMMENT '提醒类型',
  `reminder_channel` varchar(100) NOT NULL COMMENT '提醒渠道',
  `recipient` varchar(100) NOT NULL COMMENT '接收人',
  `sent_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `reminder_status` varchar(20) DEFAULT 'success' COMMENT '发送状态：success-成功，failed-失败，pending-待发送',
  `failure_reason` text DEFAULT NULL COMMENT '失败原因',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`reminder_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_sent_at` (`sent_at`),
  KEY `idx_reminder_status` (`reminder_status`),
  FOREIGN KEY (`schedule_id`) REFERENCES `repayment_schedule` (`schedule_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒日志表';

-- 插入测试用户数据（密码为：123456）
INSERT INTO `users` (`username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '管理员', 'admin@example.com', '13800138000', 1),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '普通用户', 'user@example.com', '13800138001', 1);

-- 插入测试客户数据
INSERT INTO `customers` (`full_name`, `id_number`, `contact_phone`, `email`, `address`) VALUES
('张三', '110101199001011234', '13800138000', 'zhangsan@example.com', '北京市朝阳区'),
('李四', '110101199002022345', '13800138001', 'lisi@example.com', '上海市浦东新区'),
('王五', '110101199003033456', '13800138002', 'wangwu@example.com', '广州市天河区');

-- 插入测试合同数据
INSERT INTO `loan_contracts` (`customer_id`, `loan_amount`, `annual_interest_rate`, `start_date`, `end_date`, `interest_type`, `repayment_frequency`, `contract_status`) VALUES
(1, 100000.00, 5.50, '2024-01-01', '2024-12-31', 'fixed', 'monthly', 'active'),
(2, 200000.00, 6.00, '2024-02-01', '2025-01-31', 'fixed', 'quarterly', 'active'),
(3, 150000.00, 5.80, '2024-03-01', '2025-02-28', 'variable', 'monthly', 'active');

-- 插入测试还款计划数据
INSERT INTO `repayment_schedule` (`contract_id`, `period_start`, `period_end`, `due_date`, `calculated_interest`, `period_status`) VALUES
(1, '2024-01-01', '2024-01-31', '2024-02-01', 458.33, 'pending'),
(1, '2024-02-01', '2024-02-29', '2024-03-01', 458.33, 'pending'),
(1, '2024-03-01', '2024-03-31', '2024-04-01', 458.33, 'paid'),
(2, '2024-02-01', '2024-04-30', '2024-05-01', 3000.00, 'pending'),
(2, '2024-05-01', '2024-07-31', '2024-08-01', 3000.00, 'pending'),
(3, '2024-03-01', '2024-03-31', '2024-04-01', 725.00, 'overdue');

-- 插入测试提醒日志数据
INSERT INTO `reminder_logs` (`schedule_id`, `reminder_type`, `reminder_channel`, `recipient`, `reminder_status`) VALUES
(1, 'payment_reminder', 'sms,email', '13800138000', 'success'),
(2, 'payment_reminder', 'sms', '13800138000', 'success'),
(6, 'overdue_reminder', 'sms,email,phone', '13800138002', 'failed');

-- 验证数据
SELECT 'Users' as table_name, COUNT(*) as count FROM `users`
UNION ALL
SELECT 'Customers' as table_name, COUNT(*) as count FROM `customers`
UNION ALL
SELECT 'Loan Contracts' as table_name, COUNT(*) as count FROM `loan_contracts`
UNION ALL
SELECT 'Repayment Schedules' as table_name, COUNT(*) as count FROM `repayment_schedule`
UNION ALL
SELECT 'Reminder Logs' as table_name, COUNT(*) as count FROM `reminder_logs`; 