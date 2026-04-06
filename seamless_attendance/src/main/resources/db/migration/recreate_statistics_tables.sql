-- =====================================================
-- 重新创建统计表 - 确保与Mapper字段完全一致
-- 执行此脚本将删除现有统计表并创建新表
-- =====================================================

-- 禁用外键检查（避免删除表时出现外键约束错误）
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 删除现有统计表 ====================
DROP TABLE IF EXISTS user_daily_stats;
DROP TABLE IF EXISTS user_weekly_stats;
DROP TABLE IF EXISTS user_monthly_stats;
DROP TABLE IF EXISTS user_yearly_stats;
DROP TABLE IF EXISTS studio_daily_stats;
DROP TABLE IF EXISTS studio_weekly_stats;
DROP TABLE IF EXISTS studio_monthly_stats;
DROP TABLE IF EXISTS studio_yearly_stats;

-- ==================== 创建用户每日统计表 ====================
CREATE TABLE user_daily_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    date DATE NOT NULL COMMENT '统计日期 (yyyy-MM-dd)',
    work_hours DOUBLE DEFAULT 0 COMMENT '工作时长(小时)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    attendance_status VARCHAR(20) COMMENT '出勤状态：active(在勤), late(迟到), leave(离开), absent(缺勤), holiday(假期), excused(请假)',
    check_in_time TIME COMMENT '上班打卡时间 (HH:mm:ss)',
    check_out_time TIME COMMENT '下班打卡时间 (HH:mm:ss)',
    late_minutes INT DEFAULT 0 COMMENT '迟到分钟数',
    early_leave_minutes INT DEFAULT 0 COMMENT '早退分钟数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_number, date),
    INDEX idx_user_number (user_number),
    INDEX idx_date (date)
) COMMENT '用户每日统计表';

-- ==================== 创建用户每周统计表 ====================
CREATE TABLE user_weekly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    week_start_date DATE NOT NULL COMMENT '周开始日期 (yyyy-MM-dd)',
    week_end_date DATE NOT NULL COMMENT '周结束日期 (yyyy-MM-dd)',
    total_work_days INT DEFAULT 0 COMMENT '总工作天数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    absent_count INT DEFAULT 0 COMMENT '缺勤次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_week (user_number, week_start_date),
    INDEX idx_user_number (user_number),
    INDEX idx_week_start_date (week_start_date)
) COMMENT '用户每周统计表';

-- ==================== 创建用户每月统计表 ====================
CREATE TABLE user_monthly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    total_work_days INT DEFAULT 0 COMMENT '总工作天数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    absent_count INT DEFAULT 0 COMMENT '缺勤次数',
    leave_days INT DEFAULT 0 COMMENT '请假天数',
    overtime_hours DOUBLE DEFAULT 0 COMMENT '加班小时数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_month (user_number, year, month),
    INDEX idx_user_number (user_number),
    INDEX idx_year_month (year, month)
) COMMENT '用户每月统计表';

-- ==================== 创建用户每年统计表 ====================
CREATE TABLE user_yearly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    year INT NOT NULL COMMENT '年份',
    total_work_days INT DEFAULT 0 COMMENT '总工作天数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    absent_count INT DEFAULT 0 COMMENT '缺勤次数',
    leave_days INT DEFAULT 0 COMMENT '请假天数',
    overtime_hours DOUBLE DEFAULT 0 COMMENT '加班小时数',
    performance_score DOUBLE DEFAULT 0 COMMENT '绩效评分(0-100)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_year (user_number, year),
    INDEX idx_user_number (user_number),
    INDEX idx_year (year)
) COMMENT '用户每年统计表';

-- ==================== 创建工作室每日统计表 ====================
CREATE TABLE studio_daily_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_id BIGINT NOT NULL COMMENT '工作室ID',
    date DATE NOT NULL COMMENT '统计日期 (yyyy-MM-dd)',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    present_users INT DEFAULT 0 COMMENT '出勤用户数',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_rate DOUBLE DEFAULT 0 COMMENT '活跃率(百分比)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均工作时长',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_date (studio_id, date),
    INDEX idx_studio_id (studio_id),
    INDEX idx_date (date)
) COMMENT '工作室每日统计表';

-- ==================== 创建工作室每周统计表 ====================
CREATE TABLE studio_weekly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_id BIGINT NOT NULL COMMENT '工作室ID',
    week_start_date DATE NOT NULL COMMENT '周开始日期 (yyyy-MM-dd)',
    week_end_date DATE NOT NULL COMMENT '周结束日期 (yyyy-MM-dd)',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    average_attendance_rate DOUBLE DEFAULT 0 COMMENT '平均出勤率(百分比)',
    average_activity_rate DOUBLE DEFAULT 0 COMMENT '平均活跃率(百分比)',
    total_late_count INT DEFAULT 0 COMMENT '总迟到次数',
    total_early_leave_count INT DEFAULT 0 COMMENT '总早退次数',
    total_absent_count INT DEFAULT 0 COMMENT '总缺勤次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_week (studio_id, week_start_date),
    INDEX idx_studio_id (studio_id),
    INDEX idx_week_start_date (week_start_date)
) COMMENT '工作室每周统计表';

-- ==================== 创建工作室每月统计表 ====================
CREATE TABLE studio_monthly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_id BIGINT NOT NULL COMMENT '工作室ID',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    average_attendance_rate DOUBLE DEFAULT 0 COMMENT '平均出勤率(百分比)',
    average_activity_rate DOUBLE DEFAULT 0 COMMENT '平均活跃率(百分比)',
    total_late_count INT DEFAULT 0 COMMENT '总迟到次数',
    total_early_leave_count INT DEFAULT 0 COMMENT '总早退次数',
    total_absent_count INT DEFAULT 0 COMMENT '总缺勤次数',
    total_leave_days INT DEFAULT 0 COMMENT '总请假天数',
    total_overtime_hours DOUBLE DEFAULT 0 COMMENT '总加班小时数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_month (studio_id, year, month),
    INDEX idx_studio_id (studio_id),
    INDEX idx_year_month (year, month)
) COMMENT '工作室每月统计表';

-- ==================== 创建工作室每年统计表 ====================
CREATE TABLE studio_yearly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_id BIGINT NOT NULL COMMENT '工作室ID',
    year INT NOT NULL COMMENT '年份',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    average_attendance_rate DOUBLE DEFAULT 0 COMMENT '平均出勤率(百分比)',
    average_activity_rate DOUBLE DEFAULT 0 COMMENT '平均活跃率(百分比)',
    total_late_count INT DEFAULT 0 COMMENT '总迟到次数',
    total_early_leave_count INT DEFAULT 0 COMMENT '总早退次数',
    total_absent_count INT DEFAULT 0 COMMENT '总缺勤次数',
    total_leave_days INT DEFAULT 0 COMMENT '总请假天数',
    total_overtime_hours DOUBLE DEFAULT 0 COMMENT '总加班小时数',
    performance_score DOUBLE DEFAULT 0 COMMENT '绩效评分(0-100)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_year (studio_id, year),
    INDEX idx_studio_id (studio_id),
    INDEX idx_year (year)
) COMMENT '工作室每年统计表';

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 表结构验证 ====================
SELECT '统计表创建完成' AS message;