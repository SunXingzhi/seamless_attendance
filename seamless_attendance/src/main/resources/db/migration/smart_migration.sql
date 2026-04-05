-- =====================================================
-- 智能迁移脚本 - 兼容所有MySQL版本
-- =====================================================
--
-- 此脚本使用存储过程来安全地添加/删除列
-- 避免了IF NOT EXISTS语法兼容性问题
-- =====================================================

DELIMITER //

-- 存储过程：安全添加列
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(255),
    IN columnName VARCHAR(255),
    IN columnDefinition TEXT
)
BEGIN
    DECLARE columnCount INT DEFAULT 0;

    -- 检查列是否存在
    SELECT COUNT(*) INTO columnCount
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = tableName
    AND COLUMN_NAME = columnName;

    -- 如果列不存在，则添加
    IF columnCount = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

-- 存储过程：安全删除列
CREATE PROCEDURE DropColumnIfExists(
    IN tableName VARCHAR(255),
    IN columnName VARCHAR(255)
)
BEGIN
    DECLARE columnCount INT DEFAULT 0;

    -- 检查列是否存在
    SELECT COUNT(*) INTO columnCount
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = tableName
    AND COLUMN_NAME = columnName;

    -- 如果列存在，则删除
    IF columnCount > 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' DROP COLUMN ', columnName);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- 执行安全的列操作
CALL AddColumnIfNotExists('device', 'device_id', 'VARCHAR(50) COMMENT \'设备ID\'');
CALL AddColumnIfNotExists('device', 'personnels', 'TEXT COMMENT \'人员配对信息JSON格式\'');

CALL AddColumnIfNotExists('studio', 'description', 'TEXT COMMENT \'工作室描述\'');
CALL AddColumnIfNotExists('studio', 'devices', 'TEXT COMMENT \'设备列表JSON格式\'');
CALL AddColumnIfNotExists('studio', 'admin_name', 'VARCHAR(50) COMMENT \'工作室管理员姓名\'');
CALL AddColumnIfNotExists('studio', 'admin_user_number', 'VARCHAR(50) COMMENT \'工作室管理员工号\'');

CALL DropColumnIfExists('device', 'device_type');
CALL DropColumnIfExists('device', 'studio_id');
CALL DropColumnIfExists('device', 'location');
CALL DropColumnIfExists('device', 'ip_address');

CALL DropColumnIfExists('studio', 'location');

CALL AddColumnIfNotExists('user', 'password', 'VARCHAR(100) COMMENT \'登录密码\'');
CALL AddColumnIfNotExists('user', 'studio_admin_id', 'INT COMMENT \'如果是工作室管理员，记录所属工作室ID\'');

-- user_status表列操作
CALL AddColumnIfNotExists('user_status', 'last_active_time', 'VARCHAR(20) COMMENT \'最后在线时间，格式：yyyy-MM-dd HH:mm:ss\'');
CALL AddColumnIfNotExists('user_status', 'last_absent_time', 'VARCHAR(20) COMMENT \'最后离线时间，格式：yyyy-MM-dd HH:mm:ss\'');
CALL AddColumnIfNotExists('user_status', 'today_work_hours', 'DOUBLE COMMENT \'今日工作小时数\'');
CALL AddColumnIfNotExists('user_status', 'check_in_time', 'VARCHAR(10) COMMENT \'今日打卡时间，格式：HH:mm:ss\'');
CALL AddColumnIfNotExists('user_status', 'check_out_time', 'VARCHAR(10) COMMENT \'今日下班时间，格式：HH:mm:ss\'');

-- 修改现有列
ALTER TABLE studio MODIFY COLUMN studio_code VARCHAR(50) COMMENT '工作室代码';

-- 创建统计表（如果不存在）
CREATE TABLE IF NOT EXISTS user_status (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    current_status VARCHAR(20) COMMENT '当前状态：active(在线), absent(离线)',
    last_active_time VARCHAR(20) COMMENT '最后在线时间，格式：yyyy-MM-dd HH:mm:ss',
    last_absent_time VARCHAR(20) COMMENT '最后离线时间，格式：yyyy-MM-dd HH:mm:ss',
    today_work_hours DOUBLE COMMENT '今日工作小时数',
    check_in_time VARCHAR(10) COMMENT '今日打卡时间，格式：HH:mm:ss',
    check_out_time VARCHAR(10) COMMENT '今日下班时间，格式：HH:mm:ss',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_user_number (user_number)
) COMMENT '用户状态表';

-- 创建统计表
CREATE TABLE IF NOT EXISTS user_daily_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    date DATE NOT NULL COMMENT '统计日期',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    active_hours DOUBLE DEFAULT 0 COMMENT '活跃时长(小时)',
    check_in_time TIME COMMENT '上班打卡时间',
    check_out_time TIME COMMENT '下班打卡时间',
    attendance_status VARCHAR(20) COMMENT '出勤状态：present(出勤), absent(缺勤), late(迟到), early_leave(早退), leave(请假)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_number, date),
    INDEX idx_user_number (user_number),
    INDEX idx_date (date)
) COMMENT '用户每日统计表';

CREATE TABLE IF NOT EXISTS user_weekly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    week_start DATE NOT NULL COMMENT '周开始日期',
    week_end DATE NOT NULL COMMENT '周结束日期',
    total_work_days INT DEFAULT 0 COMMENT '应出勤天数',
    actual_work_days INT DEFAULT 0 COMMENT '实际出勤天数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均每日工作时长',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    absent_count INT DEFAULT 0 COMMENT '缺勤次数',
    leave_count INT DEFAULT 0 COMMENT '请假次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_week (user_number, week_start),
    INDEX idx_user_number (user_number),
    INDEX idx_week_start (week_start)
) COMMENT '用户周统计表';

CREATE TABLE IF NOT EXISTS user_monthly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    total_work_days INT DEFAULT 0 COMMENT '应出勤天数',
    actual_work_days INT DEFAULT 0 COMMENT '实际出勤天数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均每日工作时长',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    absent_count INT DEFAULT 0 COMMENT '缺勤次数',
    leave_count INT DEFAULT 0 COMMENT '请假次数',
    full_attendance_days INT DEFAULT 0 COMMENT '满勤天数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_month (user_number, year, month),
    INDEX idx_user_number (user_number),
    INDEX idx_year_month (year, month)
) COMMENT '用户月统计表';

CREATE TABLE IF NOT EXISTS user_yearly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_number VARCHAR(50) NOT NULL COMMENT '用户工号',
    year INT NOT NULL COMMENT '年份',
    total_work_days INT DEFAULT 0 COMMENT '应出勤天数',
    actual_work_days INT DEFAULT 0 COMMENT '实际出勤天数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均每日工作时长',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_score DOUBLE DEFAULT 0 COMMENT '活跃度评分(0-100)',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    absent_count INT DEFAULT 0 COMMENT '缺勤次数',
    leave_count INT DEFAULT 0 COMMENT '请假次数',
    full_attendance_months INT DEFAULT 0 COMMENT '满勤月数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_year (user_number, year),
    INDEX idx_user_number (user_number),
    INDEX idx_year (year)
) COMMENT '用户年统计表';

CREATE TABLE IF NOT EXISTS studio_daily_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_code VARCHAR(50) NOT NULL COMMENT '工作室代码',
    date DATE NOT NULL COMMENT '统计日期',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    active_users INT DEFAULT 0 COMMENT '活跃用户数',
    present_users INT DEFAULT 0 COMMENT '出勤用户数',
    absent_users INT DEFAULT 0 COMMENT '缺勤用户数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均工作时长',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_rate DOUBLE DEFAULT 0 COMMENT '活跃率(百分比)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_date (studio_code, date),
    INDEX idx_studio_code (studio_code),
    INDEX idx_date (date)
) COMMENT '工作室每日统计表';

CREATE TABLE IF NOT EXISTS studio_weekly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_code VARCHAR(50) NOT NULL COMMENT '工作室代码',
    week_start DATE NOT NULL COMMENT '周开始日期',
    week_end DATE NOT NULL COMMENT '周结束日期',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    average_active_users DOUBLE DEFAULT 0 COMMENT '平均活跃用户数',
    average_present_users DOUBLE DEFAULT 0 COMMENT '平均出勤用户数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均工作时长',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_rate DOUBLE DEFAULT 0 COMMENT '活跃率(百分比)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_week (studio_code, week_start),
    INDEX idx_studio_code (studio_code),
    INDEX idx_week_start (week_start)
) COMMENT '工作室周统计表';

CREATE TABLE IF NOT EXISTS studio_monthly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_code VARCHAR(50) NOT NULL COMMENT '工作室代码',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    average_active_users DOUBLE DEFAULT 0 COMMENT '平均活跃用户数',
    average_present_users DOUBLE DEFAULT 0 COMMENT '平均出勤用户数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均工作时长',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_rate DOUBLE DEFAULT 0 COMMENT '活跃率(百分比)',
    full_attendance_users INT DEFAULT 0 COMMENT '满勤用户数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_month (studio_code, year, month),
    INDEX idx_studio_code (studio_code),
    INDEX idx_year_month (year, month)
) COMMENT '工作室月统计表';

CREATE TABLE IF NOT EXISTS studio_yearly_stats (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    studio_code VARCHAR(50) NOT NULL COMMENT '工作室代码',
    year INT NOT NULL COMMENT '年份',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    average_active_users DOUBLE DEFAULT 0 COMMENT '平均活跃用户数',
    average_present_users DOUBLE DEFAULT 0 COMMENT '平均出勤用户数',
    total_work_hours DOUBLE DEFAULT 0 COMMENT '总工作时长(小时)',
    average_work_hours DOUBLE DEFAULT 0 COMMENT '平均工作时长',
    attendance_rate DOUBLE DEFAULT 0 COMMENT '出勤率(百分比)',
    activity_rate DOUBLE DEFAULT 0 COMMENT '活跃率(百分比)',
    full_attendance_users INT DEFAULT 0 COMMENT '满勤用户数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_studio_year (studio_code, year),
    INDEX idx_studio_code (studio_code),
    INDEX idx_year (year)
) COMMENT '工作室年统计表';

-- 清理存储过程
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;