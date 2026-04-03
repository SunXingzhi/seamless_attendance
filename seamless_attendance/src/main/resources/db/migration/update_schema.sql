-- 更新设备表结构，添加personnels字段
ALTER TABLE device
ADD COLUMN IF NOT EXISTS device_id VARCHAR(50) COMMENT '设备ID',
ADD COLUMN IF NOT EXISTS personnels TEXT COMMENT '人员配对信息JSON格式';

-- 更新工作室表结构，修改字段类型
ALTER TABLE studio
MODIFY COLUMN studio_code VARCHAR(50) COMMENT '工作室代码',
ADD COLUMN IF NOT EXISTS description TEXT COMMENT '工作室描述',
ADD COLUMN IF NOT EXISTS devices TEXT COMMENT '设备列表JSON格式';

-- 更新设备表，删除不需要的字段（如果存在）
ALTER TABLE device
DROP COLUMN IF EXISTS device_type,
DROP COLUMN IF EXISTS studio_id,
DROP COLUMN IF EXISTS location,
DROP COLUMN IF EXISTS ip_address;

-- 更新工作室表，删除不需要的字段（如果存在）
ALTER TABLE studio
DROP COLUMN IF EXISTS location;

-- 更新用户表，添加密码字段和管理员工作室ID
ALTER TABLE user
ADD COLUMN IF NOT EXISTS password VARCHAR(100) COMMENT '登录密码',
ADD COLUMN IF NOT EXISTS studio_admin_id INT COMMENT '如果是工作室管理员，记录所属工作室ID';

-- 更新工作室表，添加管理员名称和工号字段
ALTER TABLE studio
ADD COLUMN IF NOT EXISTS admin_name VARCHAR(50) COMMENT '工作室管理员姓名',
ADD COLUMN IF NOT EXISTS admin_user_number VARCHAR(50) COMMENT '工作室管理员工号';

-- 创建 user_status 表（如果不存在）
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

-- 更新 user_status 表，添加缺失的字段（如果表已存在但字段缺失）
ALTER TABLE user_status
ADD COLUMN IF NOT EXISTS last_active_time VARCHAR(20) COMMENT '最后在线时间，格式：yyyy-MM-dd HH:mm:ss',
ADD COLUMN IF NOT EXISTS last_absent_time VARCHAR(20) COMMENT '最后离线时间，格式：yyyy-MM-dd HH:mm:ss',
ADD COLUMN IF NOT EXISTS today_work_hours DOUBLE COMMENT '今日工作小时数',
ADD COLUMN IF NOT EXISTS check_in_time VARCHAR(10) COMMENT '今日打卡时间，格式：HH:mm:ss',
ADD COLUMN IF NOT EXISTS check_out_time VARCHAR(10) COMMENT '今日下班时间，格式：HH:mm:ss';
