-- =====================================================
-- 兼容性检查脚本 - 用于MySQL 5.0+
-- =====================================================
--
-- 在执行主迁移脚本前，先运行此脚本检查哪些列已存在
-- 然后手动注释掉或删除已存在的列操作
-- =====================================================

-- 检查device表结构
DESCRIBE device;

-- 检查studio表结构
DESCRIBE studio;

-- 检查user表结构
DESCRIBE user;

-- 检查user_status表结构（如果存在）
DESCRIBE user_status;

-- 示例：如何检查特定列是否存在
-- SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = 'your_database_name'
-- AND TABLE_NAME = 'device'
-- AND COLUMN_NAME = 'device_id';