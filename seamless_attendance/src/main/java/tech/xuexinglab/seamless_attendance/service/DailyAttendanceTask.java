package tech.xuexinglab.seamless_attendance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xuexinglab.seamless_attendance.mapper.attendanceMapper;
import tech.xuexinglab.seamless_attendance.entity.attendanceRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DailyAttendanceTask {
    private static final Logger logger = LoggerFactory.getLogger(DailyAttendanceTask.class);
    
    @Autowired
    private attendanceMapper attendanceMapper;
    
    // 日期时间格式器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 每天凌晨00:00:00执行初始化任务
    @Scheduled(cron = "0 0 0 * * ?")
    public void initializeDailyAttendance() {
        try {
            logger.info("Starting daily attendance initialization task");
            
            // 获取当前时间
            LocalDateTime currentTime = LocalDateTime.now();
            String currentDateTime = currentTime.format(DATETIME_FORMATTER);
            
            // 重置用户状态
            int resetCount = attendanceMapper.resetDailyUserStatus(currentDateTime);
            logger.info("Reset daily user status for {} users", resetCount);
            
            logger.info("Daily attendance initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during daily attendance initialization", e);
        }
    }
    
    // 每天晚上23:59:59执行考勤统计任务
    @Scheduled(cron = "59 59 23 * * ?")
    public void calculateDailyAttendance() {
        try {
            logger.info("Starting daily attendance calculation task");
            
            // 这里可以添加额外的考勤统计逻辑，例如：
            // 1. 检查未打卡的用户
            // 2. 计算最终的工作时间
            // 3. 更新考勤状态
            
            logger.info("Daily attendance calculation completed successfully");
        } catch (Exception e) {
            logger.error("Error during daily attendance calculation", e);
        }
    }
}
