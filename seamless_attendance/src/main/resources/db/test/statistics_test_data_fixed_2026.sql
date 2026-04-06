-- =====================================================
-- 统计功能测试数据脚本（固定日期：2026-04-06）
-- 为前端统计页面提供完整的测试数据
-- 此脚本使用固定日期，确保数据始终可用
-- =====================================================

-- 设置固定基准日期
SET @base_date = '2026-04-06';

-- 清空现有测试数据（可选）
-- DELETE FROM user_yearly_stats;
-- DELETE FROM user_monthly_stats;
-- DELETE FROM user_weekly_stats;
-- DELETE FROM user_daily_stats;
-- DELETE FROM studio_yearly_stats;
-- DELETE FROM studio_monthly_stats;
-- DELETE FROM studio_weekly_stats;
-- DELETE FROM studio_daily_stats;

-- ==================== 用户每日统计测试数据 ====================
-- 插入最近7天的测试数据（3个用户）
INSERT INTO user_daily_stats (user_number, date, work_hours, activity_score, attendance_status, check_in_time, check_out_time, late_minutes, early_leave_minutes, create_time, update_time) VALUES
-- 用户222024322012010 - 正常出勤
('222024322012010', DATE_SUB(@base_date, INTERVAL 6 DAY), 8.5, 85.0, 'active', '08:45:00', '17:30:00', 0, 0, NOW(), NOW()),
('222024322012010', DATE_SUB(@base_date, INTERVAL 5 DAY), 7.8, 78.0, 'active', '08:50:00', '17:20:00', 10, 0, NOW(), NOW()),
('222024322012010', DATE_SUB(@base_date, INTERVAL 4 DAY), 8.2, 82.0, 'active', '08:30:00', '17:00:00', 0, 0, NOW(), NOW()),
('222024322012010', DATE_SUB(@base_date, INTERVAL 3 DAY), 6.5, 65.0, 'leave', '09:30:00', '16:00:00', 90, 120, NOW(), NOW()),
('222024322012010', DATE_SUB(@base_date, INTERVAL 2 DAY), 8.0, 80.0, 'active', '08:55:00', '17:10:00', 5, 0, NOW(), NOW()),
('222024322012010', DATE_SUB(@base_date, INTERVAL 1 DAY), 8.3, 83.0, 'active', '08:40:00', '17:15:00', 0, 15, NOW(), NOW()),
('222024322012010', @base_date, 8.1, 81.0, 'active', '08:35:00', '17:05:00', 0, 0, NOW(), NOW()),

-- 用户222023310042003 - 有迟到早退
('222023310042003', DATE_SUB(@base_date, INTERVAL 6 DAY), 7.0, 70.0, 'late', '09:30:00', '16:30:00', 90, 90, NOW(), NOW()),
('222023310042003', DATE_SUB(@base_date, INTERVAL 5 DAY), 8.0, 80.0, 'active', '08:45:00', '17:00:00', 0, 0, NOW(), NOW()),
('222024322012010', DATE_SUB(@base_date, INTERVAL 4 DAY), 0.0, 0.0, 'absent', NULL, NULL, 0, 0, NOW(), NOW()),
('222023310042003', DATE_SUB(@base_date, INTERVAL 3 DAY), 6.0, 60.0, 'leave', '10:00:00', '16:00:00', 120, 120, NOW(), NOW()),
('222023310042003', DATE_SUB(@base_date, INTERVAL 2 DAY), 8.5, 85.0, 'active', '08:30:00', '17:20:00', 0, 0, NOW(), NOW()),
('222023310042003', DATE_SUB(@base_date, INTERVAL 1 DAY), 7.8, 78.0, 'late', '09:15:00', '17:00:00', 75, 0, NOW(), NOW()),
('222023310042003', @base_date, 8.2, 82.0, 'active', '08:40:00', '17:10:00', 0, 10, NOW(), NOW()),

-- 用户003 - 请假和假期
('222024322220099', DATE_SUB(@base_date, INTERVAL 6 DAY), 0.0, 5.0, 'holiday', NULL, NULL, 0, 0, NOW(), NOW()),
('222024322220099', DATE_SUB(@base_date, INTERVAL 5 DAY), 0.0, 10.0, 'excused', NULL, NULL, 0, 0, NOW(), NOW()),
('222024322220099', DATE_SUB(@base_date, INTERVAL 4 DAY), 8.0, 80.0, 'active', '08:50:00', '17:10:00', 10, 0, NOW(), NOW()),
('222024322220099', DATE_SUB(@base_date, INTERVAL 3 DAY), 8.2, 82.0, 'active', '08:45:00', '17:15:00', 0, 15, NOW(), NOW()),
('222024322220099', DATE_SUB(@base_date, INTERVAL 2 DAY), 7.5, 75.0, 'active', '09:00:00', '17:00:00', 60, 0, NOW(), NOW()),
('222024322220099', DATE_SUB(@base_date, INTERVAL 1 DAY), 0.0, 10.0, 'excused', NULL, NULL, 0, 0, NOW(), NOW()),
('222024322220099', @base_date, 8.0, 80.0, 'active', '08:55:00', '17:05:00', 5, 0, NOW(), NOW());

-- ==================== 用户每周统计测试数据 ====================
-- 插入最近4周的测试数据
INSERT INTO user_weekly_stats (user_number, week_start_date, week_end_date, total_work_days, total_work_hours, attendance_rate, activity_score, late_count, early_leave_count, absent_count, create_time, update_time) VALUES
-- 用户222024322012010
('222024322012010', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 21 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 15 DAY), '%Y-%m-%d'), 5, 40.5, 100.0, 81.0, 1, 1, 0, NOW(), NOW()),
('222024322012010', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 14 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 8 DAY), '%Y-%m-%d'), 5, 39.0, 100.0, 78.0, 2, 0, 0, NOW(), NOW()),
('222024322012010', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 7 DAY), '%Y-%m-%d'), DATE_FORMAT(@base_date, '%Y-%m-%d'), 5, 40.0, 100.0, 80.0, 1, 1, 0, NOW(), NOW()),

-- 用户222023310042003
('222023310042003', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 21 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 15 DAY), '%Y-%m-%d'), 4, 30.0, 80.0, 60.0, 2, 1, 1, NOW(), NOW()),
('222023310042003', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 14 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 8 DAY), '%Y-%m-%d'), 5, 38.0, 100.0, 76.0, 1, 0, 0, NOW(), NOW()),
('222023310042003', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 7 DAY), '%Y-%m-%d'), DATE_FORMAT(@base_date, '%Y-%m-%d'), 4, 32.0, 80.0, 64.0, 1, 1, 1, NOW(), NOW()),

-- 用户003
('222024322220099', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 21 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 15 DAY), '%Y-%m-%d'), 3, 24.0, 60.0, 48.0, 1, 0, 2, NOW(), NOW()),
('222024322220099', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 14 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 8 DAY), '%Y-%m-%d'), 4, 32.0, 80.0, 64.0, 0, 1, 1, NOW(), NOW()),
('222024322220099', DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 7 DAY), '%Y-%m-%d'), DATE_FORMAT(@base_date, '%Y-%m-%d'), 5, 39.0, 100.0, 78.0, 2, 0, 0, NOW(), NOW());

-- ==================== 用户每月统计测试数据 ====================
-- 插入最近3个月的测试数据
INSERT INTO user_monthly_stats (user_number, month, year, total_work_days, total_work_hours, attendance_rate, activity_score, late_count, early_leave_count, absent_count, leave_days, overtime_hours, create_time, update_time) VALUES
-- 用户222024322012010
('222024322012010', MONTH(DATE_SUB(@base_date, INTERVAL 2 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 2 MONTH)), 22, 176.0, 100.0, 88.0, 3, 2, 0, 0, 8.0, NOW(), NOW()),
('222024322012010', MONTH(DATE_SUB(@base_date, INTERVAL 1 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 1 MONTH)), 21, 168.0, 95.5, 84.0, 2, 1, 1, 0, 5.0, NOW(), NOW()),
('222024322012010', MONTH(@base_date), YEAR(@base_date), 15, 120.0, 100.0, 80.0, 1, 1, 0, 0, 3.0, NOW(), NOW()),

-- 用户222023310042003
('222023310042003', MONTH(DATE_SUB(@base_date, INTERVAL 2 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 2 MONTH)), 20, 150.0, 90.9, 75.0, 5, 3, 2, 0, 2.0, NOW(), NOW()),
('222023310042003', MONTH(DATE_SUB(@base_date, INTERVAL 1 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 1 MONTH)), 19, 152.0, 86.4, 76.0, 4, 2, 3, 0, 4.0, NOW(), NOW()),
('222023310042003', MONTH(@base_date), YEAR(@base_date), 12, 96.0, 85.7, 72.0, 2, 1, 2, 0, 1.0, NOW(), NOW()),

-- 用户003
('222024322220099', MONTH(DATE_SUB(@base_date, INTERVAL 2 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 2 MONTH)), 18, 144.0, 81.8, 72.0, 3, 1, 4, 0, 0.0, NOW(), NOW()),
('222024322220099', MONTH(DATE_SUB(@base_date, INTERVAL 1 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 1 MONTH)), 20, 160.0, 90.9, 80.0, 2, 2, 2, 0, 2.0, NOW(), NOW()),
('222024322220099', MONTH(@base_date), YEAR(@base_date), 13, 104.0, 92.9, 74.0, 1, 0, 1, 0, 1.0, NOW(), NOW());

-- ==================== 用户每年统计测试数据 ====================
-- 插入最近3年的测试数据
INSERT INTO user_yearly_stats (user_number, year, total_work_days, total_work_hours, attendance_rate, activity_score, late_count, early_leave_count, absent_count, leave_days, overtime_hours, performance_score, create_time, update_time) VALUES
-- 用户222024322012010
('222024322012010', YEAR(@base_date) - 2, 240, 1920.0, 95.0, 85.0, 12, 8, 10, 5, 40.0, 85.0, NOW(), NOW()),
('222024322012010', YEAR(@base_date) - 1, 245, 1960.0, 96.0, 86.0, 10, 6, 8, 3, 45.0, 87.0, NOW(), NOW()),
('222024322012010', YEAR(@base_date), 120, 960.0, 97.0, 88.0, 5, 3, 3, 1, 20.0, 89.0, NOW(), NOW()),

-- 用户222023310042003
('222023310042003', YEAR(@base_date) - 2, 220, 1760.0, 90.0, 75.0, 25, 15, 20, 10, 25.0, 75.0, NOW(), NOW()),
('222023310042003', YEAR(@base_date) - 1, 230, 1840.0, 92.0, 77.0, 20, 12, 15, 8, 30.0, 78.0, NOW(), NOW()),
('222023310042003', YEAR(@base_date), 115, 920.0, 93.0, 79.0, 10, 5, 8, 4, 15.0, 80.0, NOW(), NOW()),

-- 用户003
('222024322220099', YEAR(@base_date) - 2, 210, 1680.0, 88.0, 70.0, 30, 10, 25, 15, 20.0, 72.0, NOW(), NOW()),
('222024322220099', YEAR(@base_date) - 1, 225, 1800.0, 91.0, 73.0, 25, 8, 20, 12, 25.0, 75.0, NOW(), NOW()),
('222024322220099', YEAR(@base_date), 110, 880.0, 92.0, 76.0, 12, 4, 10, 6, 12.0, 78.0, NOW(), NOW());

-- ==================== 工作室每日统计测试数据 ====================
-- 工作室ID为1的工作室
INSERT INTO studio_daily_stats (studio_id, date, total_users, present_users, attendance_rate, activity_rate, average_work_hours, late_count, early_leave_count, create_time, update_time) VALUES
(1, DATE_SUB(@base_date, INTERVAL 6 DAY), 3, 2, 66.7, 70.0, 7.5, 1, 1, NOW(), NOW()),
(1, DATE_SUB(@base_date, INTERVAL 5 DAY), 3, 3, 100.0, 80.0, 8.0, 0, 0, NOW(), NOW()),
(1, DATE_SUB(@base_date, INTERVAL 4 DAY), 3, 2, 66.7, 75.0, 7.8, 1, 0, NOW(), NOW()),
(1, DATE_SUB(@base_date, INTERVAL 3 DAY), 3, 1, 33.3, 65.0, 6.5, 1, 1, NOW(), NOW()),
(1, DATE_SUB(@base_date, INTERVAL 2 DAY), 3, 3, 100.0, 85.0, 8.2, 0, 0, NOW(), NOW()),
(1, DATE_SUB(@base_date, INTERVAL 1 DAY), 3, 2, 66.7, 78.0, 7.9, 1, 1, NOW(), NOW()),
(1, @base_date, 3, 3, 100.0, 82.0, 8.1, 0, 0, NOW(), NOW());

-- ==================== 工作室每周统计测试数据 ====================
INSERT INTO studio_weekly_stats (studio_id, week_start_date, week_end_date, total_users, average_attendance_rate, average_activity_rate, total_late_count, total_early_leave_count, total_absent_count, create_time, update_time) VALUES
(1, DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 21 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 15 DAY), '%Y-%m-%d'), 3, 85.7, 75.0, 3, 2, 4, NOW(), NOW()),
(1, DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 14 DAY), '%Y-%m-%d'), DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 8 DAY), '%Y-%m-%d'), 3, 90.5, 78.0, 2, 1, 2, NOW(), NOW()),
(1, DATE_FORMAT(DATE_SUB(@base_date, INTERVAL 7 DAY), '%Y-%m-%d'), DATE_FORMAT(@base_date, '%Y-%m-%d'), 3, 88.9, 80.0, 1, 1, 3, NOW(), NOW());

-- ==================== 工作室每月统计测试数据 ====================
INSERT INTO studio_monthly_stats (studio_id, month, year, total_users, average_attendance_rate, average_activity_rate, total_late_count, total_early_leave_count, total_absent_count, total_leave_days, total_overtime_hours, create_time, update_time) VALUES
(1, MONTH(DATE_SUB(@base_date, INTERVAL 2 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 2 MONTH)), 3, 88.0, 76.0, 8, 5, 10, 5, 15.0, NOW(), NOW()),
(1, MONTH(DATE_SUB(@base_date, INTERVAL 1 MONTH)), YEAR(DATE_SUB(@base_date, INTERVAL 1 MONTH)), 3, 90.0, 78.0, 6, 4, 8, 3, 18.0, NOW(), NOW()),
(1, MONTH(@base_date), YEAR(@base_date), 3, 92.0, 80.0, 3, 2, 5, 2, 12.0, NOW(), NOW());

-- ==================== 工作室每年统计测试数据 ====================
INSERT INTO studio_yearly_stats (studio_id, year, total_users, average_attendance_rate, average_activity_rate, total_late_count, total_early_leave_count, total_absent_count, total_leave_days, total_overtime_hours, performance_score, create_time, update_time) VALUES
(1, YEAR(@base_date) - 2, 3, 85.0, 72.0, 35, 20, 40, 25, 60.0, 75.0, NOW(), NOW()),
(1, YEAR(@base_date) - 1, 3, 88.0, 75.0, 28, 16, 32, 20, 70.0, 78.0, NOW(), NOW()),
(1, YEAR(@base_date), 3, 90.0, 78.0, 15, 8, 18, 12, 45.0, 82.0, NOW(), NOW());

-- ==================== 数据验证查询 ====================
SELECT '用户每日统计记录数:' AS description, COUNT(*) AS count FROM user_daily_stats
UNION ALL
SELECT '用户每周统计记录数:', COUNT(*) FROM user_weekly_stats
UNION ALL
SELECT '用户每月统计记录数:', COUNT(*) FROM user_monthly_stats
UNION ALL
SELECT '用户每年统计记录数:', COUNT(*) FROM user_yearly_stats
UNION ALL
SELECT '工作室每日统计记录数:', COUNT(*) FROM studio_daily_stats
UNION ALL
SELECT '工作室每周统计记录数:', COUNT(*) FROM studio_weekly_stats
UNION ALL
SELECT '工作室每月统计记录数:', COUNT(*) FROM studio_monthly_stats
UNION ALL
SELECT '工作室每年统计记录数:', COUNT(*) FROM studio_yearly_stats;